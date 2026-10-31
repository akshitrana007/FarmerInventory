package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MandiPriceService {

    // Your data.gov.in API key and the resource ID for the mandi price dataset
    private static final String API_KEY = "579b464db66ec23bdd0000016292fccb6f654cbe5d05e026c2bf1ed6";
    private static final String RESOURCE_ID = "35985678-0d79-46b4-9ed6-6f13308a1d24";

    // The API gives prices per quintal (100 kg). We convert to per-kg
    // right away so every other part of the app can assume "price = per kg".
    private static final double KG_PER_QUINTAL = 100.0;

    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();
    static {
        CATEGORY_MAP.put("Potato", "Vegetable");
        CATEGORY_MAP.put("Onion", "Vegetable");
        CATEGORY_MAP.put("Tomato", "Vegetable");
        CATEGORY_MAP.put("Cabbage", "Vegetable");
        CATEGORY_MAP.put("Cauliflower", "Vegetable");
        CATEGORY_MAP.put("Brinjal", "Vegetable");
        CATEGORY_MAP.put("Wheat", "Cereal");
        CATEGORY_MAP.put("Rice", "Cereal");
        CATEGORY_MAP.put("Maize", "Cereal");
        CATEGORY_MAP.put("Bajra", "Cereal");
        CATEGORY_MAP.put("Mango", "Fruit");
        CATEGORY_MAP.put("Banana", "Fruit");
        CATEGORY_MAP.put("Apple", "Fruit");
        CATEGORY_MAP.put("Grapes", "Fruit");
    }

    /**
     * Calls the Agmarknet API for the given commodity + state, converts the
     * quintal price to a per-kg price, saves it into mandi_prices, and
     * returns the per-kg price.
     * Returns null if the state is missing, the API has no data, or something fails.
     */
    public Double fetchAndCacheMandiPrice(String commodity, String state) {

        if (state == null || state.trim().isEmpty()) {
            System.out.println("No state set for this farmer — cannot fetch mandi price.");
            return null;
        }

        try {
            String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
            String encodedCommodity = URLEncoder.encode(commodity, StandardCharsets.UTF_8);

            String url = "https://api.data.gov.in/resource/" + RESOURCE_ID
                    + "?api-key=" + API_KEY
                    + "&format=json"
                    + "&filters[State]=" + encodedState
                    + "&filters[Commodity]=" + encodedCommodity
                    + "&sort[Arrival_Date]=desc"
                    + "&limit=1";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray records = root.getAsJsonArray("records");

            if (records == null || records.size() == 0) {
                System.out.println("No mandi price data found for " + commodity + " in " + state);
                return null;
            }

            JsonObject latest = records.get(0).getAsJsonObject();
            double modalPricePerQuintal = Double.parseDouble(latest.get("Modal_Price").getAsString());
            double pricePerKg = modalPricePerQuintal / KG_PER_QUINTAL;

            saveToCache(commodity, state, pricePerKg);

            return pricePerKg;

        } catch (IOException | InterruptedException e) {
            System.out.println("Error calling mandi price API: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Error parsing mandi price response: " + e.getMessage());
            return null;
        }
    }

    // Saves (or updates) the fetched per-kg price into the mandi_prices cache table
    private void saveToCache(String commodity, String state, double pricePerKg) {
        String category = CATEGORY_MAP.getOrDefault(commodity, "Other");

        String sql = "INSERT INTO mandi_prices (crop_name, category, area, price, fetched_at) " +
                "VALUES (?, ?, ?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE price = VALUES(price), fetched_at = NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, commodity);
            stmt.setString(2, category);
            stmt.setString(3, state);
            stmt.setDouble(4, pricePerKg);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving mandi price to cache: " + e.getMessage());
        }
    }
}