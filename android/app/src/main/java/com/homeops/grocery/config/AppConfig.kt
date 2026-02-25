package com.homeops.grocery.config

/**
 * Central configuration file for the HomeOps Grocery Android app.
 *
 * Change BASE_URL to match the IP address and port of your miniPC running the backend.
 * Example: "http://192.168.1.42:8000/"
 *
 * Note: Cleartext traffic is allowed in the manifest for development convenience.
 *       Use HTTPS and a proper certificate in production.
 */
object AppConfig {
    /**
     * Base URL for the HomeOps Grocery backend API.
     * Must end with a trailing slash.
     */
    const val BASE_URL = "http://192.168.1.100:8000/"
}
