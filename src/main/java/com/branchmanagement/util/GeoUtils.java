package com.branchmanagement.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for geographical calculations.
 * Implements the Haversine formula to calculate distance between two GPS coordinates.
 */
@UtilityClass
public class GeoUtils {

    private static final int EARTH_RADIUS_METERS = 6371000;

    /**
     * Calculates the distance between two points on Earth using their latitude and longitude.
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in meters, or Double.MAX_VALUE if any coordinate is null
     */
    public static double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}