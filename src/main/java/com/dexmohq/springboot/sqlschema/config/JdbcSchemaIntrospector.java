package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.DatabaseMetadataIntegrator;
import com.dexmohq.springboot.sqlschema.model.Schema;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.DatabaseMetaData;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class JdbcSchemaIntrospector {

    private final List<EntityManager> entityManagers;

    @Autowired
    public JdbcSchemaIntrospector(List<EntityManager> entityManagers) {
        this.entityManagers = entityManagers;
    }

    public Schema introspect(String persistenceUnit) {
        final EntityManager entityManager = entityManagers.stream()
                .filter(em -> persistenceUnit.equals(em.getEntityManagerFactory().unwrap(SessionFactory.class).getProperties().get("hibernate.ejb.persistenceUnitName")))
                .findAny()
                .orElseThrow();

        return entityManager.unwrap(Session.class).doReturningWork(connection -> {
            final DatabaseMetaData metaData = connection.getMetaData();
            final String catalogName = DatabaseMetadataIntegrator.getMetadataMap().get(persistenceUnit).getDatabase().getJdbcEnvironment().getCurrentCatalog().getText();
            return JdbcSchemaUtils.fromMetadata(metaData, catalogName);
        });
    }

}
