package com.dexmohq.springboot.sqlschema.config;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@SuppressWarnings("UnusedReturnValue")
public class ExposeSchemaPropertiesBuilder {

    private final ExposeSchemaProperties properties;

    public ExposeSchemaPropertiesBuilder persistenceUnits(String... units) {
        this.properties.getPersistenceUnits().addAll(Arrays.asList(units));
        return this;
    }

    public ExposeSchemaPropertiesBuilder exclude(Class<?>... excludedEntities) {
        for (final Class<?> excludedEntity : excludedEntities) {
            this.properties.getExclude().add(excludedEntity.getName());
        }
        return this;
    }

    public ExposeSchemaPropertiesBuilder include(Class<?>... includedEntities) {
        for (final Class<?> includedEntity : includedEntities) {
            this.properties.getInclude().add(includedEntity.getName());
        }
        return this;
    }

    public ExposeSchemaPropertiesBuilder basePath(String basePath) {
        properties.setBasePath(basePath);
        return this;
    }

    public ExposeSchemaPropertiesBuilder introspect(boolean introspect) {
        properties.setIntrospect(introspect);
        return this;
    }

}
