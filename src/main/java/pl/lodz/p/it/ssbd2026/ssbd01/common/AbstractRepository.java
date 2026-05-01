package pl.lodz.p.it.ssbd2026.ssbd01.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface AbstractRepository<T> extends JpaRepository<T, UUID> {
//    T saveAndFlush(T entity);
}