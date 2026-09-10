package com.yash.thecroncher.core.ports

/**
 * Persistence, abstracted away from SharedPreferences so settings logic is
 * testable on the JVM. `:app` supplies the Android-backed implementation.
 */
interface SettingsStore {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
    fun getInt(key: String, default: Int): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
}

/** In-memory store: the test double, and a safe fallback if storage is missing. */
class InMemorySettingsStore : SettingsStore {
    private val values = HashMap<String, Any>()

    override fun getString(key: String, default: String) = values[key] as? String ?: default
    override fun putString(key: String, value: String) { values[key] = value }
    override fun getInt(key: String, default: Int) = values[key] as? Int ?: default
    override fun putInt(key: String, value: Int) { values[key] = value }
    override fun getBoolean(key: String, default: Boolean) = values[key] as? Boolean ?: default
    override fun putBoolean(key: String, value: Boolean) { values[key] = value }
}
