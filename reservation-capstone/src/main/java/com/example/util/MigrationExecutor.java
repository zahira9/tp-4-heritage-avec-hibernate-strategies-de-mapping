package com.example.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MigrationExecutor {
    private static final Logger LOGGER = Logger.getLogger(MigrationExecutor.class.getName());
    private final String url;
    private final String username;
    private final String password;

    public MigrationExecutor(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void execute(String resourcePath) {
        List<String> statements = readStatements(resourcePath);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
            LOGGER.info("Migration exécutée : " + resourcePath);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Échec de migration : " + resourcePath, e);
            throw new IllegalStateException("Impossible d'exécuter la migration " + resourcePath, e);
        }
    }

    private List<String> readStatements(String resourcePath) {
        InputStream resource = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Script de migration introuvable : " + resourcePath);
        }
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String sqlLine = line.replaceFirst("--.*$", "").trim();
                if (!sqlLine.isEmpty()) {
                    current.append(sqlLine).append(' ');
                    if (sqlLine.endsWith(";")) {
                        statements.add(current.substring(0, current.length() - 1).trim());
                        current.setLength(0);
                    }
                }
            }
            if (!current.toString().trim().isEmpty()) {
                statements.add(current.toString().trim());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire la migration " + resourcePath, e);
        }
        return statements;
    }
}
