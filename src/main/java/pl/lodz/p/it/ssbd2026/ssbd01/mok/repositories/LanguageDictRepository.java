package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public interface LanguageDictRepository extends AbstractRepository<LanguageDict> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Optional<LanguageDict> findByLanguage(String language);
}
