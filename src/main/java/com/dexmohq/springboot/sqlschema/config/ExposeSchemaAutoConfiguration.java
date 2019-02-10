package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.DatabaseMetadataIntegrator;
import com.dexmohq.springboot.sqlschema.model.Schema;
import com.dexmohq.springboot.sqlschema.model.Schemas;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.boot.Metadata;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Configuration
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(ExposeSchemaProperties.class)
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@CommonsLog
@RequiredArgsConstructor
public class ExposeSchemaAutoConfiguration {

    private final ExposeSchemaProperties properties;
    private final JdbcSchemaIntrospector jdbcSchemaIntrospector;

    private Schema buildSchema(String persistenceUnit, Metadata metadata) {
        if (properties.isIntrospect()) {
            return jdbcSchemaIntrospector.introspect(persistenceUnit);
        }
        return HibernateSchemaUtils.fromMetadata(metadata, properties);
    }

    @Bean
    Schemas schemas() {
        final Map<String, Schema> exposed = DatabaseMetadataIntegrator.getMetadataMap().entrySet().stream()
                .filter(e -> properties.getPersistenceUnits().isEmpty() || properties.getPersistenceUnits().contains(e.getKey()))
                .map(e -> Map.entry(e.getKey(), buildSchema(e.getKey(), e.getValue())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Schemas(exposed);
    }

}
