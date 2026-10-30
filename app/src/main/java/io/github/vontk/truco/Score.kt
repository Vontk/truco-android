package io.github.vontk.truco

internal enum class Team { US, THEM }

internal data class Score(val us: Int = 0, val them: Int = 0) {
    init { require(us in 0..30 && them in 0..30) }
    fun change(team: Team, delta: Int): Score {
        require(delta == -1 || delta == 1)
        return when (team) {
            Team.US -> copy(us = (us + delta).coerceIn(0, 30))
            Team.THEM -> copy(them = (them + delta).coerceIn(0, 30))
        }
    }
}

/** Three traditional squares in each phase; the diagonal is always the fifth stroke. */
internal fun tallyGroups(score: Int, good: Boolean): List<Int> {
    val points = if (good) (score - 15).coerceIn(0, 15) else score.coerceIn(0, 15)
    return List(3) { (points - it * 5).coerceIn(0, 5) }
}
