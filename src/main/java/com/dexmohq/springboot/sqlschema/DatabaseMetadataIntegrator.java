package com.dexmohq.springboot.sqlschema;

import lombok.Getter;
import lombok.Value;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DatabaseMetadataIntegrator implements Integrator {

    @Getter
    private static final Map<String, List<String>> metadataMap = new HashMap<>();

    @Getter
    private static Schema schema;

    @Getter
    private static Database database;

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        final Database db = metadata.getDatabase();
        database = db;

        final String dialect = db.getDialect().getClass().getSimpleName().replaceFirst("Dialect$", "");
        final List<Namespace> namespaces = StreamSupport.stream(db.getNamespaces().spliterator(), false)
                .collect(Collectors.toList());

        schema = new Schema(dialect, namespaces);

        for (final Namespace namespace : metadata.getDatabase().getNamespaces()) {
            for (final Table table : namespace.getTables()) {
                @SuppressWarnings("unchecked") final List<String> columns = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize((Iterator<Column>) table.getColumnIterator(), Spliterator.ORDERED), false)
                        .map(Column::getName)
                        .collect(Collectors.toList());
                metadataMap.put(table.getName(), columns);
            }
        }
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        // nothing to disintegrate
    }

    @Value
    public static class Schema {
        String dialect;
        List<Namespace> namespaces;
    }

}