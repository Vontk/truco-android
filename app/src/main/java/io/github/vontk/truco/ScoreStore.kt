package io.github.vontk.truco

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.scoreDataStore by preferencesDataStore(name = "score")

/** One atomic DataStore transaction per tap; writes survive Activity/process recreation. */
internal class ScoreStore(context: Context) {
    private val store = context.applicationContext.scoreDataStore
    private val us = intPreferencesKey("us")
    private val them = intPreferencesKey("them")
    private fun read(p: Preferences) = Score(
        (p[us] ?: 0).coerceIn(0, 30), (p[them] ?: 0).coerceIn(0, 30)
    )
    val scores = store.data.map(::read)
    suspend fun change(team: Team, delta: Int) {
        store.edit { p ->
            val next = read(p).change(team, delta)
            p[us] = next.us
            p[them] = next.them
        }
    }
    suspend fun reset() { store.edit { it[us] = 0; it[them] = 0 } }
}
