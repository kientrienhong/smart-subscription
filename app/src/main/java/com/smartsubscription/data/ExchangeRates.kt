package com.smartsubscription.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** How many units of each currency one [base] buys, e.g. base USD: {"VND": 26300.0, "EUR": 0.92}. */
data class ExchangeRates(val base: String, val perBase: Map<String, Double>)

/**
 * Today's rates from open.er-api.com: free, no key, updated daily, and it covers VND, which the ECB feeds do not.
 * Only the currency code is sent. Returns null offline or on any bad reply, and the totals then fall back to one per currency.
 */
suspend fun fetchExchangeRates(base: String): ExchangeRates? = withContext(Dispatchers.IO) {
    try {
        val conn = URL("https://open.er-api.com/v6/latest/$base").openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        try {
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            if (json.optString("result") != "success") return@withContext null
            val rates = json.getJSONObject("rates")
            ExchangeRates(base, rates.keys().asSequence().associateWith { rates.getDouble(it) }.filterValues { it > 0 })
        } finally {
            conn.disconnect()
        }
    } catch (_: Exception) {
        null
    }
}
