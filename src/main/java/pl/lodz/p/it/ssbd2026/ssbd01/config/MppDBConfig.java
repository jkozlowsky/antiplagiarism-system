package pl.lodz.p.it.ssbd2026.ssbd01.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateSettings;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
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
        em.setPackagesToScan("pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities",
                "pl.lodz.p.it.ssbd2026.ssbd01.mok.entities");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }
}

