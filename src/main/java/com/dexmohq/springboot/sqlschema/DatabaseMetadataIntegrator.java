package com.dexmohq.springboot.sqlschema;

import lombok.Data;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.HashMap;
import java.util.Map;

@CommonsLog
@Data
public class DatabaseMetadataIntegrator implements Integrator {

    public static final DatabaseMetadataIntegrator INSTANCE = new DatabaseMetadataIntegrator();

    @Getter
    private static Map<String, Metadata> metadataMap = new HashMap<>();

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        metadataMap.put(((String) sessionFactoryImplementor.getProperties().get("hibernate.ejb.persistenceUnitName")), metadata);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        // nothing to disintegrate
    }

}