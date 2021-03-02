package com.github.wickoo.obsidianapi.mysql;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.Properties;

public class SQLConnection {

    private JavaPlugin plugin;

    private Connection connection = null;
    private String host, port, database, username, password;
    private Properties properties;

    public SQLConnection (JavaPlugin plugin) {

        this.plugin = plugin;

        this.host = plugin.getConfig().getString("host");
        this.port = plugin.getConfig().getString("port");
        this.database = plugin.getConfig().getString("database");
        this.username = plugin.getConfig().getString("username");
        this.password = plugin.getConfig().getString("password");

        properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("useSSL", "true");
        properties.setProperty("autoReconnect", "true");

    }

    public void openConnection() throws SQLException {

        if (connection != null && !connection.isClosed()) {
            return;
        }

        connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, properties);


    }

    public void refreshConnection() {

        Statement st = null;
        ResultSet valid = null;
        try {
            st = connection.createStatement();
            valid = st.executeQuery("SELECT 1 FROM Dual");
            if (valid.next())
                return;
        } catch (SQLException e2) {
            System.out.println("Connection is idle or terminated. Reconnecting...");
        }

        long start = 0;
        long end = 0;

        try {
            start = System.currentTimeMillis();
            System.out.println("Attempting to establish a connection the MySQL server!");
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, properties);
            end = System.currentTimeMillis();
            System.out.println("Connection to MySQL server established! (" + host + ":" + port + ")");
            System.out.println("Connection took " + ((end - start)) + "ms!");
        } catch (SQLException e) {
            System.out.println("Could not connect to MySQL server! because: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found!");
        }

    }

    public Connection getConnection() throws SQLException {

        refreshConnection();
        return connection;
    }

}

