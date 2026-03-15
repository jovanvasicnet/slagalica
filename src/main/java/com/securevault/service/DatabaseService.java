package com.securevault.service;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseService {

    public static Connection connect() throws Exception {

        String url = System.getenv("postgresql://neondb_owner:npg_5FulOvSi2sLn@ep-snowy-bird-age3fggs-pooler.c-2.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require");

        return DriverManager.getConnection(url);
    }
}
