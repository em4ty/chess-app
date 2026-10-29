package com.example.kp

import android.content.Context

/**
 * Единственное место, где хранятся данные авторизации.
 * Раньше часть кода читала "chess_auth", а часть "chess_prefs", из-за чего user_id терялся.
 */
object AuthStore {

    private const val PREFS = "chess_auth"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"

    private fun prefs() = ChessApp.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun token(): String? = prefs().getString(KEY_TOKEN, null)

    fun userId(): Long = prefs().getLong(KEY_USER_ID, 0L)

    fun username(): String? = prefs().getString(KEY_USERNAME, null)

    fun isLoggedIn(): Boolean = userId() > 0 && token() != null

    fun save(response: LoginResponse) {
        prefs().edit()
            .putString(KEY_TOKEN, response.token)
            .putLong(KEY_USER_ID, response.user.id)
            .putString(KEY_USERNAME, response.user.username)
            .apply()
    }

    fun clear() {
        prefs().edit().clear().apply()
    }
}
