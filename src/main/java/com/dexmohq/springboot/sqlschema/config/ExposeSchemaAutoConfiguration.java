package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.DatabaseMetadataIntegrator;
import com.dexmohq.springboot.sqlschema.controller.SchemaController;
import com.dexmohq.springboot.sqlschema.model.Schema;
import com.dexmohq.springboot.sqlschema.model.Schemas;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.boot.Metadata;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Configuration
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(ExposeSchemaProperties.class)
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@CommonsLog
@RequiredArgsConstructor
@ComponentScan(basePackageClasses = SchemaController.class)
public class ExposeSchemaAutoConfiguration implements InitializingBean {

    private final ExposeSchemaProperties properties;
    private final List<ExposeSchemaConfigurer> exposeSchemaConfigurers;

    @Bean
    @ConditionalOnMissingBean
    public JdbcSchemaIntrospectionService jdbcSchemaIntrospector(List<EntityManager> entityManagers) {
        return new JdbcSchemaIntrospectionService(entityManagers);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    public SchemaController schemaController(Schemas schemas) {
        return new SchemaController(schemas);
    }

    private Schema buildSchema(String persistenceUnit, Metadata metadata, JdbcSchemaIntrospectionService jdbcSchemaIntrospectionService) {
        if (properties.isIntrospect()) {
            return jdbcSchemaIntrospectionService.introspect(persistenceUnit);
        }
        return HibernateSchemaUtils.fromMetadata(metadata, properties);
    }

    @Bean
    Schemas schemas(JdbcSchemaIntrospectionService jdbcSchemaIntrospectionService) {
        final Map<String, Schema> exposed = DatabaseMetadataIntegrator.getMetadataMap().entrySet().stream()
                .filter(e -> properties.getPersistenceUnits().isEmpty() || properties.getPersistenceUnits().contains(e.getKey()))
                .map(e -> Map.entry(e.getKey(), buildSchema(e.getKey(), e.getValue(), jdbcSchemaIntrospectionService)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Schemas(exposed);
    }

    @Override
    public void afterPropertiesSet() {
        final ExposeSchemaPropertiesBuilder config = new ExposeSchemaPropertiesBuilder(properties);
        for (final ExposeSchemaConfigurer exposeSchemaConfigurer : exposeSchemaConfigurers) {
            exposeSchemaConfigurer.configure(config);
        }
    }
}
