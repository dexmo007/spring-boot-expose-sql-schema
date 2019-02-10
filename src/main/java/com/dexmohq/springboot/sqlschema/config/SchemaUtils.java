package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.model.Schema;
import lombok.experimental.UtilityClass;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;

import java.util.List;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class SchemaUtils {

    Schema fromMetadata(Metadata metadata, ExposeSchemaProperties properties) {
        final Database db = metadata.getDatabase();
        final String dialect = db.getDialect().getClass().getSimpleName().replaceFirst("Dialect$", "");
        final List<Table> exposedPersistentClasses = metadata.getEntityBindings().stream()
                .filter(entityClass -> properties.getInclude().isEmpty() || properties.getInclude().contains(entityClass.getClassName()))
                .filter(entityClass -> !properties.getExclude().contains(entityClass.getClassName()))
                .filter(entityClass -> properties.getInclude().contains(entityClass.getClassName())
                        || entityClass.getMappedClass().getAnnotation(NoExposure.class) == null)
                .map(PersistentClass::getTable)
                .collect(toList());
        return new Schema(dialect, exposedPersistentClasses);
    }


}
