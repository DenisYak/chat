package ru.geekbrains;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/data2.db");
        stmt = connection.createStatement();
    }

    public static String getNickByLoginPass(String login, String pass) {
        try {
            ResultSet rs = stmt.executeQuery(String.format("SELECT nick FROM users WHERE login = '%s' AND password = '%s';", login, pass));
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addNewUser(String login, String pass, String nick) {
        try {
            stmt.executeUpdate(String.format("INSERT INTO users (login, password, nick) VALUES ('%s', '%s', '%s');", login, pass, nick));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
