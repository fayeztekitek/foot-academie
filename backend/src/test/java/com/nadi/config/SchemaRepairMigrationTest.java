package com.nadi.config;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the startup repair widens legacy varchar(255) photo columns to
 * TEXT (the cause of HTTP 500 on every photo save) and is idempotent.
 */
class SchemaRepairMigrationTest {

    private DataSource h2() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:schemarepair" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        return ds;
    }

    private String columnType(DataSource ds, String table, String column) throws Exception {
        try (Connection c = ds.getConnection();
             ResultSet rs = c.getMetaData().getColumns(null, null, table.toUpperCase(), column.toUpperCase())) {
            assertTrue(rs.next(), "column should exist");
            return rs.getString("TYPE_NAME") + "(" + rs.getLong("COLUMN_SIZE") + ")";
        }
    }

    private static boolean isWidened(String type) {
        // PostgreSQL reports "text"; H2 maps TEXT to CHARACTER VARYING(10^9).
        // Either way the 255-char ceiling must be gone.
        String upper = type.toUpperCase();
        if (upper.contains("TEXT") || upper.contains("CLOB") || upper.contains("OBJECT")) {
            return true;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\((\\d+)\\)").matcher(upper);
        return m.find() && Long.parseLong(m.group(1)) > 255;
    }

    @Test
    void widensLegacyVarcharColumnsToText() throws Exception {
        DataSource ds = h2();
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE joueur (id BIGINT PRIMARY KEY, photo_url VARCHAR(255), postes_secondaires VARCHAR(255))");
            s.execute("CREATE TABLE academie (id BIGINT PRIMARY KEY, logo_url VARCHAR(255))");
            s.execute("CREATE TABLE document (id BIGINT PRIMARY KEY, fichier_url VARCHAR(255))");
            s.execute("CREATE TABLE entraineur (id BIGINT PRIMARY KEY, photo_url VARCHAR(255))");
        }
        String before = columnType(ds, "joueur", "photo_url").toUpperCase();
        assertTrue(before.contains("VARCHAR") || before.contains("VARYING"),
                "expected varchar-like type but was " + before);

        new SchemaRepairMigration(ds).run(null);

        String widened = columnType(ds, "joueur", "photo_url").toUpperCase();
        assertTrue(isWidened(widened),
                "expected widened type but was " + widened);
    }

    @Test
    void secondRunIsANoop() throws Exception {
        DataSource ds = h2();
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE joueur (id BIGINT PRIMARY KEY, photo_url VARCHAR(255), postes_secondaires VARCHAR(255))");
            s.execute("CREATE TABLE academie (id BIGINT PRIMARY KEY, logo_url VARCHAR(255))");
            s.execute("CREATE TABLE document (id BIGINT PRIMARY KEY, fichier_url VARCHAR(255))");
            s.execute("CREATE TABLE entraineur (id BIGINT PRIMARY KEY, photo_url VARCHAR(255))");
        }

        SchemaRepairMigration migration = new SchemaRepairMigration(ds);
        migration.run(null);

        assertDoesNotThrow(() -> migration.run(null));
        String widened = columnType(ds, "entraineur", "photo_url").toUpperCase();
        assertTrue(isWidened(widened), "expected widened type but was " + widened);
    }

    @Test
    void missingTablesDoNotFailStartup() {
        DataSource ds = h2();

        assertDoesNotThrow(() -> new SchemaRepairMigration(ds).run(null));
    }
}
