package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface BaseTestRepository<T> extends JpaRepository<T, UUID> {
}
