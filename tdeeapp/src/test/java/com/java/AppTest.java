package com.java;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AppTest 
{
    @Test
    public void testDatabaseConnection() {
        DatabaseManager dbManager = new DatabaseManager();
        boolean isConnected = dbManager.connect();
        assertTrue(isConnected, "La connexion à la base de données doit réussir");
    }
}
