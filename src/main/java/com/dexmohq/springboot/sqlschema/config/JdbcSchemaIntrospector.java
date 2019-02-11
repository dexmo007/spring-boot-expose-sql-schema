package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.model.*;
import org.hibernate.dialect.Dialect;

import java.sql.*;
import java.util.*;

public class JdbcSchemaIntrospector {

    private final Connection connection;
    private final DatabaseMetaData metaData;
    private final String tableCatalog;
    private final Dialect dialect;

    public JdbcSchemaIntrospector(Connection connection, String tableCatalog, Dialect dialect) throws SQLException {
        this.connection = connection;
        this.metaData = connection.getMetaData();
        this.tableCatalog = tableCatalog;
        this.dialect = dialect;
    }

    private Set<ColumnDef> retrieveColumnDefs(String tableSchema,
                                              String tableName,
                                              PrimaryKeyDef primaryKey,
                                              Set<IndexDef> indexes) throws SQLException {
        final Set<ColumnDef> columnDefs = new HashSet<>();
        final ResultSet columns = metaData.getColumns(tableCatalog, tableSchema, tableName, "%");
        while (columns.next()) {
            final String name = columns.getString("COLUMN_NAME");
            final String type = columns.getString("TYPE_NAME");
            final int size = columns.getInt("COLUMN_SIZE");
            final boolean nullable = columns.getBoolean("NULLABLE");
            final boolean unique = primaryKey.getColumns().equals(Collections.singletonList(name))
                    || indexes.stream().anyMatch(i -> i.isUnique() && i.getColumns().equals(Collections.singletonList(name)));
            columnDefs.add(new ColumnDef(name, type, size, nullable, unique, false));
        }
        return columnDefs;
    }

    private PrimaryKeyDef retrievePrimaryKey(String tableSchema, String tableName) throws SQLException {
        final ResultSet primaryKeys = metaData.getPrimaryKeys(tableCatalog, tableSchema, tableName);
        final ArrayList<String> primaryKeyColumns = new ArrayList<>();
        String pkName = null;
        while (primaryKeys.next()) {
            primaryKeyColumns.add(primaryKeys.getString("COLUMN_NAME"));
            if (pkName == null) {
                pkName = primaryKeys.getString("PK_NAME");
            }
        }
        return new PrimaryKeyDef(pkName, primaryKeyColumns);
    }

    private Set<ForeignKeyDef> retrieveForeignKeys(String tableSchema, String tableName) throws SQLException {
        final ResultSet importedKeys = metaData.getImportedKeys(tableCatalog, tableSchema, tableName);
        final HashMap<String, ForeignKeyDef> foreignKeyDefs = new HashMap<>();
        while (importedKeys.next()) {
            final String name = importedKeys.getString("FK_NAME");
            final String referencedTable = importedKeys.getString("FKTABLE_NAME");
            final ForeignKeyDef def = foreignKeyDefs.computeIfAbsent(name, n -> new ForeignKeyDef(n, new ArrayList<>(), referencedTable, new ArrayList<>()));
            def.getColumns().add(importedKeys.getString("PKCOLUMN_NAME"));
            def.getReferencedColumns().add(importedKeys.getString("FKCOLUMN_NAME"));
        }
        return new HashSet<>(foreignKeyDefs.values());
    }

    private Set<IndexDef> retrieveIndexes(String tableSchema, String tableName) throws SQLException {
        final HashMap<String, IndexDef> indexDefs = new HashMap<>();
        final ResultSet indexInfos = metaData.getIndexInfo(tableCatalog, tableSchema, tableName, false, false);
        while (indexInfos.next()) {
            final String indexName = indexInfos.getString("INDEX_NAME");
            final String columnName = indexInfos.getString("COLUMN_NAME");
            final boolean unique = !indexInfos.getBoolean("NON_UNIQUE");
            final String ascOrDesc = indexInfos.getString("ASC_OR_DESC");
            final IndexDef.Order order;
            if (ascOrDesc == null) {
                order = null;
            } else {
                switch (ascOrDesc) {
                    case "A":
                        order = IndexDef.Order.ASC;
                        break;
                    case "D":
                        order = IndexDef.Order.DESC;
                        break;
                    default:
                        order = null;
                }
            }
            final IndexDef indexDef = indexDefs.computeIfAbsent(indexName, in -> new IndexDef(in, new ArrayList<>(), unique, order));
            // todo assure correct order of columns
            indexDef.getColumns().add(columnName);
        }
        return new HashSet<>(indexDefs.values());
    }

    private Set<CheckConstraintDef> retrieveCheckConstraints(String tableSchema, String tableName) {
        // todo do all DB vendors provide the information like this?
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT CONSTRAINT_NAME, CHECK_EXPRESSION FROM INFORMATION_SCHEMA.CONSTRAINTS " +
                        "WHERE CONSTRAINT_TYPE = 'CHECK' " +
                        "AND TABLE_CATALOG = ? " +
                        "AND TABLE_SCHEMA = ? " +
                        "AND TABLE_NAME = ?")) {
            statement.setString(1, tableCatalog);
            statement.setString(2, tableSchema);
            statement.setString(3, tableName);
            try (ResultSet rs = statement.executeQuery()) {
                final HashSet<CheckConstraintDef> checkConstraints = new HashSet<>();
                while (rs.next()) {
                    checkConstraints.add(new CheckConstraintDef(
                            rs.getString("CONSTRAINT_NAME"),
                            rs.getString("CHECK_EXPRESSION")
                    ));
                }
                return checkConstraints;
            } catch (SQLException e) {
                return Collections.emptySet();
            }
        } catch (SQLException e) {
            return Collections.emptySet();
        }
    }

    public Schema introspect() throws SQLException {
        final ArrayList<TableDef> tableDefs = new ArrayList<>();

        ResultSet resultSet = metaData.getTables(tableCatalog, null, "%", new String[]{"TABLE"});
        while (resultSet.next()) {
            String tableName = resultSet.getString(3);
            String tableSchema = resultSet.getString(2);
            final PrimaryKeyDef primaryKey = retrievePrimaryKey(tableSchema, tableName);
            final Set<IndexDef> indexes = retrieveIndexes(tableSchema, tableName);
            tableDefs.add(new TableDef(
                    tableCatalog + "." + tableSchema + "." + tableName,
                    retrieveColumnDefs(tableSchema, tableName, primaryKey, indexes),
                    primaryKey,
                    retrieveCheckConstraints(tableSchema, tableName),
                    retrieveForeignKeys(tableSchema, tableName),
                    indexes
            ));
        }

        return new Schema(metaData.getDatabaseProductName(), tableDefs);
    }

}
