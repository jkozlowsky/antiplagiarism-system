package pl.lodz.p.it.ssbd2026.ssbd01.config.db;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(
        basePackages = "pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories",
        entityManagerFactoryRef = "mppEntityManagerFactory",
        transactionManagerRef = "mppTransactionManager"
)
public class MppDBConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.mpp")
    public DataSourceProperties mppDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource mppDataSource() {
        return mppDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name="mppEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mppEntityManagerFactory(
            @Qualifier("mppDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("pl.lodz.p.it.ssbd2026.ssbd01.entities");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }


    @Bean(name="mppTransactionManager")
    public PlatformTransactionManager mppTransactionManager(
            @Qualifier("mppEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}