package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.DatabaseMetadataIntegrator;
import com.dexmohq.springboot.sqlschema.model.Schema;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.relational.Database;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.DatabaseMetaData;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JdbcSchemaIntrospectionService {

    private final List<EntityManager> entityManagers;

    public Schema introspect(String persistenceUnit) {
        final EntityManager entityManager = entityManagers.stream()
                .filter(em -> persistenceUnit.equals(em.getEntityManagerFactory().unwrap(SessionFactory.class).getProperties().get("hibernate.ejb.persistenceUnitName")))
                .findAny()
                .orElseThrow();

        return entityManager.unwrap(Session.class).doReturningWork(connection -> {
            final Database database = DatabaseMetadataIntegrator.getMetadataMap().get(persistenceUnit).getDatabase();
            final String catalogName = database.getJdbcEnvironment().getCurrentCatalog().getText();
            return new JdbcSchemaIntrospector(connection, catalogName, database.getDialect()).introspect();
        });
    }

}
