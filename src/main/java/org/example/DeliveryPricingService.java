package org.example;

public class DeliveryPricingService {

    // Vehicle tiers based on weight (kg)
    public static String getVehicleForWeight(double weightKg) {
        if (weightKg <= 20) {
            return "BIKE";
        } else if (weightKg <= 40) {
            return "THREE_WHEELER";
        } else if (weightKg <= 100) {
            return "PICKUP";
        } else {
            return "MINI_TRUCK";
        }
    }

    // Calculates delivery charge based on weight (kg) and distance (km)
    public static double calculateDeliveryCharge(double weightKg, double distanceKm) {
        String vehicle = getVehicleForWeight(weightKg);
        double remainingDistance = distanceKm;
        double totalCharge = 0;

        // Rate table: [slabLimitKm, ratePerKmForThisSlab]
        double[][] rates;

        switch (vehicle) {
            case "BIKE":
                rates = new double[][]{{5, 8}, {5, 7}, {10, 6}, {Double.MAX_VALUE, 5}};
                break;
            case "THREE_WHEELER":
                rates = new double[][]{{5, 12}, {5, 10}, {10, 9}, {Double.MAX_VALUE, 8}};
                break;
            case "PICKUP":
                rates = new double[][]{{5, 18}, {5, 16}, {10, 14}, {Double.MAX_VALUE, 12}};
                break;
            default: // MINI_TRUCK — reusing PICKUP rates as a base; adjust if you want separate rates
                rates = new double[][]{{5, 18}, {5, 16}, {10, 14}, {Double.MAX_VALUE, 12}};
                break;
        }

        for (double[] slab : rates) {
            if (remainingDistance <= 0) break;
            double slabDistance = Math.min(remainingDistance, slab[0]);
            totalCharge += slabDistance * slab[1];
            remainingDistance -= slabDistance;
        }

        return totalCharge;
    }

    // Human-readable vehicle name for display in the UI
    public static String getVehicleDisplayName(String vehicleCode) {
        switch (vehicleCode) {
            case "BIKE": return "Bike 🏍️";
            case "THREE_WHEELER": return "3-Wheeler 🛺";
            case "PICKUP": return "Pickup/Jeep 🚙";
            case "MINI_TRUCK": return "Mini-Truck 🚚";
            default: return vehicleCode;
        }
    }
}