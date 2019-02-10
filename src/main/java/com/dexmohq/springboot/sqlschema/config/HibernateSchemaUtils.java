package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.model.*;
import lombok.experimental.UtilityClass;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class HibernateSchemaUtils {

    private static <E> Stream<E> stream(Iterator<E> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }

    @SuppressWarnings("unchecked")
    private ForeignKeyDef foreignKeyDefFor(ForeignKey fk) {
        return new ForeignKeyDef(fk.getName(),
                fk.getColumns().stream().map(Column::getQuotedName).collect(toList()),
                fk.getReferencedTable().getName(),
                ((List<Column>) fk.getReferencedColumns()).stream().map(Column::getQuotedName).collect(toList()));
    }

    private ColumnDef columnDefFor(Column column) {
        return new ColumnDef(
                column.getQuotedName(),
                column.getSqlType(),
                column.getLength(),
                column.isNullable(),
                column.isUnique(),
                column.isFormula()
        );
    }

    @SuppressWarnings("unchecked")
    private TableDef tableDefFor(Table table) {
        return new TableDef(
                table.getName(),
                stream((Iterator<Column>) table.getColumnIterator())
                        .map(HibernateSchemaUtils::columnDefFor)
                        .collect(Collectors.toSet()),
                new PrimaryKeyDef(table.getPrimaryKey().getName(), table.getPrimaryKey().getColumns().stream().map(Column::getQuotedName).collect(toList())),
                stream(table.getCheckConstraintsIterator()).collect(Collectors.toSet()),
                table.getForeignKeys().values().stream().map(HibernateSchemaUtils::foreignKeyDefFor).collect(Collectors.toSet()),
                Stream.concat(
                        stream(table.getUniqueKeyIterator()).map(uk -> new IndexDef(uk.getName(),
                                uk.getColumns().stream().map(Column::getQuotedName).collect(toList()), true, null)),
                        stream(table.getIndexIterator())
                                .map(index -> new IndexDef(index.getName(), stream(index.getColumnIterator()).map(Column::getQuotedName).collect(toList()), false,null)))
                        .collect(Collectors.toSet())
        );
    }

    Schema fromMetadata(Metadata metadata, ExposeSchemaProperties properties) {
        final Database db = metadata.getDatabase();
        final String dialect = db.getDialect().getClass().getSimpleName().replaceFirst("Dialect$", "");
        final List<TableDef> exposedTables = metadata.getEntityBindings().stream()
                .filter(entityClass -> properties.getInclude().isEmpty() || properties.getInclude().contains(entityClass.getClassName()))
                .filter(entityClass -> !properties.getExclude().contains(entityClass.getClassName()))
                .filter(entityClass -> properties.getInclude().contains(entityClass.getClassName())
                        || entityClass.getMappedClass().getAnnotation(NoExposure.class) == null)
                .map(PersistentClass::getTable)
                .map(HibernateSchemaUtils::tableDefFor)
                .collect(toList());
        return new Schema(dialect, exposedTables);
    }


}
