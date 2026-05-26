package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp13;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MPP13_DeleteRulePreset extends BaseTestSetup {

    @Autowired
    private RuleTestRepository ruleTestRepository;

    @Autowired
    private RaportLevelTestRepository raportLevelTestRepository;

    @Autowired
    private VersionHashService versionHashService;

    @Autowired
    @Qualifier("mppTransactionManager")
    private PlatformTransactionManager transactionManager;

    private UUID rulePresetId;
    private String validEtag;

    @BeforeEach
    public void setupData() {
        var txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.executeWithoutResult(status -> {

            RulePreset preset = new RulePreset();
            preset.setIsPreset(true);
            preset.setSubject(null);

            preset.setStudentTicketCount(10);
            preset.setMinimumTokensMatch(20);
            preset.setEnableNormalization(true);

            var iterator = raportLevelTestRepository.findAll().iterator();
            if (!iterator.hasNext()) {
                throw new IllegalStateException("Brak słowników poziomu raportowania w bazie!");
            }
            preset.setRaportLevel(iterator.next());

            ruleTestRepository.save(preset);
            rulePresetId = preset.getId();

            validEtag = versionHashService.hashObjectVersion(rulePresetId, preset.getVersion());
        });
    }

    @Test
    @DisplayName("POS01: Powinien poprawnie usunąć globalny szablon reguł (kod 204)")
    public void POS01_DeleteRulePreset_Success() {
        Headers requestHeaders = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", validEtag)
        );

        io.restassured.RestAssured.given()
                .headers(requestHeaders)
                .delete("/api/rules/" + rulePresetId)
                .then()
                .statusCode(204);

        var txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(ruleTestRepository.findById(rulePresetId).isEmpty(),
                    "Globalny szablon reguł powinien zostać trwale usunięty z bazy.");
        });
    }

    @Test
    @DisplayName("NEG01: Powinien odrzucić próbę usunięcia ze względu na błędny ETag (Conflict 409)")
    public void NEG01_DeleteRulePreset_ConcurrentUpdateException() {
        Headers requestHeaders = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", "niepoprawny-hash-wersji-12345")
        );

        io.restassured.RestAssured.given()
                .headers(requestHeaders)
                .delete("/api/rules/" + rulePresetId)
                .then()
                .statusCode(409);

        var txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(ruleTestRepository.findById(rulePresetId).isPresent(),
                    "Globalny szablon reguł nie powinien zostać usunięty z powodu błędu blokady optymistycznej.");
        });
    }
}