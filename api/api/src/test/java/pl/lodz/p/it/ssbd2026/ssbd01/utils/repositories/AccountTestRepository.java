package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

import java.util.Optional;

public interface AccountTestRepository extends BaseTestRepository<Account> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByLogin(String login);

    Account save(Account account);
}
