package com.reto.codigoton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class SingletonConnection {
    /*
    * Conexión a la base de datos usando el patrón de diseño Singleton
    */
    private static Connection conn = null;
    private static String url;
    private static String driver;
    private static String bd;
    private static String usu;
    private static String pass;

    private SingletonConnection(String url, String driver, String bd, String usu,
            String pass) throws SQLException {

        this.url = url;
        this.driver = driver;
        this.bd = bd;
        this.usu = usu;
        this.pass = pass;

        try {
            if (driver != null && !driver.isEmpty()) {
                Class.forName(driver);
            }

            url += "/" + bd;
            conn = DriverManager.getConnection(url, usu, pass);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    } 

    public static synchronized Connection getConnection(String url, String driver, String bd, String usu,
            String pass) throws SQLException {

        if (conn == null) {
            new SingletonConnection(url, driver, bd, usu, pass);
        }

        return conn;
    } 
    
    public static synchronized Connection getConnection() throws SQLException {
        if (conn == null) {
            new SingletonConnection(url, driver, bd, usu, pass);
        }

        return conn;
    } 

    public static void close() throws SQLException {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqlex) {
                System.out.println("Error closing Connection ...\n"
                        + sqlex.getMessage());
            } finally {
                conn = null;
            }
        }
    }

    public static void close(ResultSet rs) throws SQLException {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqlex) {
                System.out.println("Error closing ResultSet ...\n"
                        + sqlex.getMessage());
            } finally {
                rs = null;
            }
        }
    }

    public static void close(PreparedStatement pstm) throws SQLException {
        if (pstm != null) {
            try {
                pstm.close();
            } catch (SQLException sqlex) {
                System.out.println("Error closing PreparedStatement ...\n"
                        + sqlex.getMessage());
            } finally {
                pstm = null;
            }
        }
    }

    public static void close(Statement stm) throws SQLException {
        if (stm != null) {
            try {
                stm.close();
            } catch (SQLException sqlex) {
                System.out.println("Error closing Statement ...\n"
                        + sqlex.getMessage());
            } finally {
                stm = null;
            }
        }
    }
}
