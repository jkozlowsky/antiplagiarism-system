package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional(transactionManager = "mppTransactionManager")
public class FindRulePresetsRepositoryTest extends BaseTestSetup {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        RaportLevelDict raportLevel = entityManager.createQuery(
                        "SELECT r FROM RaportLevelDict r WHERE r.name = 'FULL_VIEW'", RaportLevelDict.class)
                .setMaxResults(1)
                .getSingleResult();

        RulePreset template = new RulePreset();
        template.setIsPreset(true);
        template.setStudentTicketCount(10);
        template.setRaportLevel(raportLevel);
        template.setMinimumTokensMatch(20);
        template.setEnableNormalization(true);
        ruleRepository.save(template);
    }

    @Test
    @DisplayName("Powinno pobrać wszystkie dostępne szablony")
    public void shouldReturnAllTemplates() {
        List<RulePreset> results = ruleRepository.findAllByIsPreset(true);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(RulePreset::getIsPreset));
    }
}