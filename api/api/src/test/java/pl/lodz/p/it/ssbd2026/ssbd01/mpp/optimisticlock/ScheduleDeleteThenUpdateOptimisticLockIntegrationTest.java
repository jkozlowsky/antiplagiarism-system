package pl.lodz.p.it.ssbd2026.ssbd01.mpp.optimisticlock;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.RulePresetTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.ScheduleTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectAssignmentTestRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Demonstracja blokady optymistycznej (@Version) dla scenariusza:
 * najpierw USUNIĘCIE krotki, a następnie RÓWNOLEGŁA jej MODYFIKACJA.
 *
 * <p>Aby zobaczyć prawdziwy {@link OptimisticLockException}, encja musi pozostać
 * ZARZĄDZANA (managed) w otwartym kontekście perzystencji, podczas gdy osobne
 * połączenie usuwa wiersz. Przy {@code flush()} Hibernate wykonuje
 * {@code UPDATE schedule ... WHERE id = ? AND version = ?}, które trafia na
 * 0 wierszy (krotka usunięta) → {@code StaleObjectStateException} →
 * {@link OptimisticLockException}.</p>
 *
 * <p>Test celowo używa surowych {@link EntityManager}-ów z
 * {@code mppEntityManagerFactory} (a nie repozytoriów Spring Data), dzięki czemu
 * aspekt {@code GenericOptimisticLockHandlingInterceptor} NIE opakowuje wyjątku
 * w {@code ConcurrentUpdateException} — widzimy dokładnie ten wyjątek JPA,
 * o który chodzi.</p>
 */
public class ScheduleDeleteThenUpdateOptimisticLockIntegrationTest extends BaseTestSetup {

    @Autowired
    private ScheduleTestRepository scheduleTestRepository;

    @Autowired
    private RulePresetTestRepository rulePresetTestRepository;

    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;

    @Autowired
    @Qualifier("mppTransactionManager")
    private PlatformTransactionManager mppTransactionManager;

    @Autowired
    @Qualifier("mppEntityManagerFactory")
    private EntityManagerFactory mppEntityManagerFactory;

    private static final String TEST_TEACHER_LOGIN = "test_teacher";

    private UUID scheduleId;

    @BeforeEach
    void createSchedule() {
        TransactionTemplate tx = new TransactionTemplate(mppTransactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        scheduleId = tx.execute(_ -> {
            SubjectAssignment assignment = subjectAssignmentTestRepository
                    .findByTeacherAccountLogin(TEST_TEACHER_LOGIN).getFirst();
            Subject subject = assignment.getSubject();
            RulePreset rulePreset = rulePresetTestRepository.findBySubjectId(subject.getId()).getFirst();

            Schedule schedule = new Schedule();
            schedule.setScheduleDateTime(LocalDateTime.now().plusDays(3));
            schedule.setTag("optimistic-lock-tag");
            schedule.setRulePreset(rulePreset);
            schedule.setModifiableUntil(LocalDateTime.now().plusDays(3));

            return scheduleTestRepository.saveAndFlush(schedule).getId();
        });
    }

    @Test
    @DisplayName("Usunięcie krotki, a następnie równoległa modyfikacja powinny rzucić OptimisticLockException")
    void deleteThenConcurrentUpdate_shouldThrowOptimisticLockException() {
        EntityManager emModifier = mppEntityManagerFactory.createEntityManager();
        EntityManager emDeleter = mppEntityManagerFactory.createEntityManager();
        try {
            // "Wątek B" (modyfikujący) odczytuje encję ZANIM nastąpi usunięcie —
            // encja pozostaje zarządzana, z wersją N.
            emModifier.getTransaction().begin();
            Schedule managed = emModifier.find(Schedule.class, scheduleId);
            assertNotNull(managed, "Harmonogram powinien istnieć przed usunięciem");

            // "Wątek A" (usuwający) — najpierw — usuwa tę samą krotkę na osobnym
            // połączeniu i commituje. Wiersz znika z bazy.
            emDeleter.getTransaction().begin();
            emDeleter.remove(emDeleter.find(Schedule.class, scheduleId));
            emDeleter.getTransaction().commit();

            // "Wątek B" próbuje teraz zapisać swoją zmianę. flush() wykona
            // UPDATE ... WHERE id = ? AND version = N → 0 wierszy → konflikt wersji.
            managed.setTag("zmiana-po-usunieciu");
            assertThrows(OptimisticLockException.class, emModifier::flush);
        } finally {
            if (emModifier.getTransaction().isActive()) {
                emModifier.getTransaction().rollback();
            }
            if (emDeleter.getTransaction().isActive()) {
                emDeleter.getTransaction().rollback();
            }
            emModifier.close();
            emDeleter.close();
        }

        // Potwierdzenie efektu końcowego: krotka faktycznie nie istnieje.
        assertNull(
                scheduleTestRepository.findById(scheduleId).orElse(null),
                "Po usunięciu i odrzuconej modyfikacji krotka nie powinna istnieć"
        );
    }
}
