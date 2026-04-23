package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<T> extends CrudRepository<T, UUID> {
}
