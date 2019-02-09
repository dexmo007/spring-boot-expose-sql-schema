package com.dexmohq.springboot.sqlschema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.QualifiedTableName;
import org.hibernate.mapping.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class JacksonConfig {


    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilder() {
        return builder -> builder
                .mixIn(Metadata.class, Ignored.class)
                .mixIn(Database.class, Ignored.class)
                .mixIn(org.hibernate.mapping.Value.class, Ignored.class)
                .mixIn(SimpleValue.class, Ignored.class)
                .mixIn(Column.class, ColumnMixin.class)
                .filters(new SimpleFilterProvider().addFilter("hibernate-orm-column-filter", SimpleBeanPropertyFilter.filterOutAllExcept(
                        "sqlType", "nullable", "unique", "formula", "name", "length"
                )))
                .mixIn(PrimaryKey.class, PrimaryKeyMixin.class)
                .mixIn(ForeignKey.class, ForeignKeyMixin.class)
                .mixIn(Table.class, TableMixin.class)
                .mixIn(QualifiedTableName.class, QualifiedTableNameMixin.class)
                .mixIn(Index.class, IndexMixin.class)
                .mixIn(UniqueKey.class, UniqueKeyMixin.class)
                .serializerByType(Properties.class, new JsonSerializer<Properties>() {
                    @Override
                    public void serialize(Properties value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        gen.writeStartObject();
                        for (final Map.Entry<Object, Object> entry : value.entrySet()) {
                            if (entry.getValue() instanceof String) {
                                gen.writeStringField((String) entry.getKey(), (String) entry.getValue());
                            }
                        }
                        gen.writeEndObject();
                    }
                })
                .serializerByType(Identifier.class, new JsonSerializer<Identifier>() {
                    @Override
                    public void serialize(Identifier value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        gen.writeString(value.render());
                    }
                })
                ;
    }

    @JsonIgnoreType
    public static class Ignored {

    }

    @JsonFilter("hibernate-orm-column-filter")
    public abstract static class ColumnMixin {
    }

    @JsonIgnoreProperties({"table", "columnIterator", "columnSpan"})
    public abstract static class PrimaryKeyMixin {

        @JsonSerialize(contentConverter = ColumnToNameConverter.class)
        public abstract List<Column> getColumns();
    }

    public static class ColumnToNameConverter extends StdConverter<Column, String> {

        @Override
        public String convert(Column value) {
            return value.getQuotedName(DatabaseMetadataIntegrator.getDatabase().getDialect());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties({"table", "columnIterator", "columnSpan", "referencedEntityName", "cascadeDeleteEnabled", "creationEnabled", "physicalConstraint"})
    public interface ForeignKeyMixin {
        @JsonSerialize(converter = TableNameConverter.class)
        Table getReferencedTable();

        @JsonSerialize(contentConverter = ColumnToNameConverter.class)
        List<Column> getColumns();

        @JsonSerialize(contentConverter = ColumnToNameConverter.class)
        List<Column> getReferencedColumns();


    }

    public static class TableNameConverter extends StdConverter<Table, QualifiedTableName> {

        @Override
        public QualifiedTableName convert(Table value) {
            return value.getQualifiedTableName();
        }
    }

    @JsonIgnoreProperties({"table", "columnOrderMap", "columnIterator", "columnSpan"})
    public interface UniqueKeyMixin {
        @JsonSerialize(contentConverter = ColumnToNameConverter.class)
        List<Column> getColumns();
    }

    @JsonIgnoreProperties({"uniqueInteger", "initCommands", "columnSpan", "catalogQuoted", "quotedName", "schemaQuoted"})
    public interface TableMixin {
        @JsonIgnore
        Map<Table.ForeignKeyKey, ForeignKey> getForeignKeys();

        @JsonProperty("foreignKeys")
        Iterator<ForeignKey> getForeignKeyIterator();

        @JsonProperty("columns")
        Iterator<Column> getColumnIterator();

        @JsonProperty("checkConstraints")
        Iterator<String> getCheckConstraintsIterator();

        @JsonProperty("indexes")
        Iterator<Index> getIndexIterator();

        @JsonProperty("uniqueKeys")
        Iterator<UniqueKey> getUniqueKeyIterator();
    }

    @JsonIgnoreProperties({"objectName"})
    public interface QualifiedTableNameMixin {

    }

    @JsonIgnoreProperties({"table", "columnOrderMap", "columnSpan"})
    public interface IndexMixin {
        @JsonProperty("columns")
        @JsonSerialize(contentConverter = ColumnToNameConverter.class)
        Iterator<Column> getColumnIterator();
    }

}
