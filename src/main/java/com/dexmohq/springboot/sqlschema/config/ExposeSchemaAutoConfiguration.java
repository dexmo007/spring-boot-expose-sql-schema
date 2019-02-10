package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.DatabaseMetadataIntegrator;
import com.dexmohq.springboot.sqlschema.model.Schema;
import com.dexmohq.springboot.sqlschema.model.Schemas;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Configuration
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(ExposeSchemaProperties.class)
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@CommonsLog
public class ExposeSchemaAutoConfiguration {

    private final ExposeSchemaProperties properties;

    @Autowired
    public ExposeSchemaAutoConfiguration(ExposeSchemaProperties properties) {
        this.properties = properties;
    }

//    @Bean
//    Metadata hibernateMetadata() {
//        return DatabaseMetadataIntegrator.INSTANCE.getMetadata();
//    }

    private String getPersistenceUnitName(EntityManager em) {
        return (String) em.getEntityManagerFactory().unwrap(SessionFactory.class).getProperties().get("hibernate.ejb.persistenceUnitName");
    }

    @Bean
    Schemas schemas() {

        final Map<String, Schema> exposed = DatabaseMetadataIntegrator.getMetadataMap().entrySet().stream()
                .filter(e -> properties.getPersistenceUnits().isEmpty() || properties.getPersistenceUnits().contains(e.getKey()))
                .map(e -> Map.entry(e.getKey(), SchemaUtils.fromMetadata(e.getValue(), properties)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Schemas(exposed);
    }

}
