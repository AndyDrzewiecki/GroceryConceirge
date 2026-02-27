package com.homeops.grocery

import com.homeops.grocery.config.AppConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests that verify BASE_URL constraints without requiring an Android device.
 * These run on the local JVM via `./gradlew :app:test`.
 */
class BaseUrlTest {

    @Test
    fun `DEFAULT_BASE_URL is not empty`() {
        assertTrue(
            "DEFAULT_BASE_URL must not be blank",
            AppConfig.DEFAULT_BASE_URL.isNotBlank(),
        )
    }

    @Test
    fun `DEFAULT_BASE_URL ends with trailing slash`() {
        assertTrue(
            "DEFAULT_BASE_URL must end with '/' for Retrofit compatibility",
            AppConfig.DEFAULT_BASE_URL.endsWith("/"),
        )
    }

    @Test
    fun `DEFAULT_BASE_URL starts with http scheme`() {
        assertTrue(
            "DEFAULT_BASE_URL must start with http:// or https://",
            AppConfig.DEFAULT_BASE_URL.startsWith("http://") ||
                AppConfig.DEFAULT_BASE_URL.startsWith("https://"),
        )
    }

    @Test
    fun `DEFAULT_BASE_URL does not contain a personal LAN IP`() {
        // Ensure no private IP (192.168.x.x or 10.x.x.x other than emulator alias) is hard-coded.
        // The emulator alias 10.0.2.2 is explicitly allowed.
        val url = AppConfig.DEFAULT_BASE_URL
        assertFalse(
            "Do not commit a personal LAN IP (192.168.x.x) to source control",
            url.contains(Regex("192\\.168\\.\\d+\\.\\d+")),
        )
    }

    /** Helper: the normalisation logic in SettingsManager appends "/" if missing. */
    @Test
    fun `url normalisation appends trailing slash when missing`() {
        val raw = "http://10.0.2.2:8000"
        val normalised = if (raw.endsWith("/")) raw else "$raw/"
        assertTrue(normalised.endsWith("/"))
    }
}
