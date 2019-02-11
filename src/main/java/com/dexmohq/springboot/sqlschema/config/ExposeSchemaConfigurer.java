package com.dexmohq.springboot.sqlschema.config;

import lombok.RequiredArgsConstructor;

public interface ExposeSchemaConfigurer {

    void configure(ExposeSchemaConfig config);

    @RequiredArgsConstructor
    class ExposeSchemaConfig {

        private final ExposeSchemaProperties exposeSchemaProperties;

        public ExposeSchemaConfig exclude(Class<?>... excludedEntities) {
            for (final Class<?> excludedEntity : excludedEntities) {
                this.exposeSchemaProperties.getExclude().add(excludedEntity.getName());
            }
            return this;
        }

    }

}
