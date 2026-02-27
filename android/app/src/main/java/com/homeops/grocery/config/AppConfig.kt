package com.homeops.grocery.config

/**
 * Default BASE_URL when the user has not yet configured a server address
 * via the in-app Settings screen.
 *
 * - "http://10.0.2.2:8000/"  →  standard Android emulator host alias.
 * - Physical phone on LAN    →  change via Settings screen at runtime.
 *   No personal IPs are committed to source control.
 *
 * The trailing slash is required by Retrofit.
 */
object AppConfig {
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
}
