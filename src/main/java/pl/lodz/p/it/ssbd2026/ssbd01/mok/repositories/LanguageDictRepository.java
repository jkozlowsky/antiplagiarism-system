package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import org.springframework.stereotype.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;

import java.util.Optional;

@Repository
public interface LanguageDictRepository extends AbstractRepository<LanguageDict> {
    Optional<LanguageDict> findByLanguage(String language);
}
