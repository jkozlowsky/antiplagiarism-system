package pl.lodz.p.it.ssbd2026.ssbd01.config.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.javers.core.Javers;
import org.javers.hibernate.integration.HibernateUnproxyObjectAccessHook;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.jpa.JpaHibernateConnectionProvider;
import org.javers.spring.jpa.TransactionalJpaJaversBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JaversConfig {

    @Bean
    public AuthorProvider provideJaversAuthor() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "SYSTEM";
            }
            return auth.getName();
        };
    }

    @Bean
    public Javers javers(
            @Qualifier("adminEntityManagerFactory") EntityManagerFactory entityManagerFactory,
            @Qualifier("adminTransactionManager") PlatformTransactionManager txManager) {

        EntityManager entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);

        JaversSqlRepository sqlRepository = SqlRepositoryBuilder
                .sqlRepository()
                .withConnectionProvider(new JpaHibernateConnectionProvider(entityManager))
                .withDialect(DialectName.POSTGRES)
                .build();

        return TransactionalJpaJaversBuilder
                .javers()
                .withTxManager(txManager)
                .registerJaversRepository(sqlRepository)
                .withObjectAccessHook(new HibernateUnproxyObjectAccessHook())
                .build();
    }
}