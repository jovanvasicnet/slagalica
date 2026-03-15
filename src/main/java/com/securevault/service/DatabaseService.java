package com.securevault.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseService {

    public static Connection connect() throws Exception {

        String url = "jdbc:postgresql://ep-snowy-bird-age3fggs-pooler.c-2.eu-central-1.aws.neon.tech/neondb?sslmode=require";
        String user = "neondb_owner";
        String password = "npg_5FulOvSi2sLn";

        Connection conn = DriverManager.getConnection(url, user, password);

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT username FROM admin");

        while(rs.next()){
            System.out.println("Admin u bazi: " + rs.getString("username"));
        }

        return conn;
    }
}
