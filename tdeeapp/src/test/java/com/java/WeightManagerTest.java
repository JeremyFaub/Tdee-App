package com.java;

import com.java.WeightManager.WeightEntry;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeightManagerTest {
    DatabaseManager dbManager = new DatabaseManager();
    WeightManager weightManager;

    @BeforeAll
    void setup() {
        dbManager.connect();
        weightManager = new WeightManager(dbManager);

        dbManager.initializeDatabase();
        dbManager.executeUpdate("DELETE FROM weight");
    }

    @Test
    void testAddWeightEntry() {
        weightManager.addWeightEntryToday(187.3, "2024-12-22");
        Double latestWeight = weightManager.getLatestWeight();
        assertNotNull(latestWeight, "getLatestWeight() returned null after adding a weight entry."); 
        assertEquals(187.3, latestWeight, 0.01);
        weightManager.deleteWeightEntry("2024-12-22");
    }

    @Test
    void testGetWeightHistory() {
        weightManager.addWeightEntryToday(170.1, "2024-12-19");
        weightManager.addWeightEntryToday(170.5, "2024-12-20");
        List<WeightEntry> history = weightManager.getWeightHistory();
        assertEquals(2, history.size());
        assertEquals(170.1, history.get(0).getValue());
        assertEquals(170.5, history.get(1).getValue());

        assertEquals("2024-12-19", history.get(0).getDate());
        assertEquals("2024-12-20", history.get(1).getDate());
        
        weightManager.deleteWeightEntry("2024-12-19");
        weightManager.deleteWeightEntry("2024-12-20");
    }

    @Test
    void testCalculateAverageWeight() {
        weightManager.addWeightEntryToday(180.0, "2024-12-21");
        weightManager.addWeightEntryToday(190.0, "2024-12-22");
        Double averageWeight = weightManager.calculateAverageWeight();
        assertNotNull(averageWeight, "calculateAverageWeight() returned null.");
        assertEquals(185.0, averageWeight, 0.01);

        weightManager.deleteWeightEntry("2024-12-21");
        weightManager.deleteWeightEntry("2024-12-22");
    }

    @Test
    void testUpdateWeightEntry() {
        weightManager.addWeightEntryToday(175.0, "2024-12-23");
        boolean updated = weightManager.updateWeightEntry(180.0, "2024-12-23");
        assertTrue(updated, "updateWeightEntry() failed to update the entry.");
        assertEquals(180.0, weightManager.getWeightOnDate("2024-12-23"), 0.01);

        weightManager.deleteWeightEntry("2024-12-23");
    }

    @Test
    void testGetWeightChange() {
        weightManager.addWeightEntryToday(160.0, "2024-12-24");
        weightManager.addWeightEntryToday(165.0, "2024-12-25");
        double weightChange = weightManager.getWeightChange("2024-12-24", "2024-12-25");
        assertEquals(5.0, weightChange, 0.01);

        weightManager.deleteWeightEntry("2024-12-24");
        weightManager.deleteWeightEntry("2024-12-25");
    }

    @Test
    void testGetWeightHistoryInRange() {
        weightManager.addWeightEntryToday(155.0, "2024-12-26");
        weightManager.addWeightEntryToday(157.0, "2024-12-27");
        List<WeightEntry> history = weightManager.getWeightHistory("2024-12-26", "2024-12-27");
        assertEquals(2, history.size());
        assertEquals(155.0, history.get(0).getValue());
        assertEquals(157.0, history.get(1).getValue());

        weightManager.deleteWeightEntry("2024-12-26");
        weightManager.deleteWeightEntry("2024-12-27");
    }
}
