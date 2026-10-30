package io.github.vontk.truco

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ScoreStore(application)
    private val mutableScore = MutableStateFlow<Score?>(null)
    val score = mutableScore.asStateFlow()
    private val errors = Channel<Unit>(Channel.CONFLATED)
    val saveErrors = errors.receiveAsFlow()
    // A single consumer preserves tap/reset order, including rapid alternating teams.
    private val commands = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    init {
        viewModelScope.launch {
            try { store.scores.collect { mutableScore.value = it } }
            catch (_: IOException) { errors.send(Unit) }
        }
        viewModelScope.launch {
            for (command in commands) {
                try { command() } catch (_: IOException) { errors.send(Unit) }
            }
        }
    }
    fun change(team: Team, delta: Int) { commands.trySend { store.change(team, delta) } }
    fun reset() { commands.trySend { store.reset() } }
}
