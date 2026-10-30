package io.github.vontk.truco

import org.junit.Assert.*
import org.junit.Test

class ScoreTest {
    @Test fun limitsAndTeamIsolation() {
        for (team in Team.entries) {
            var score = Score()
            repeat(50) { score = score.change(team, 1) }
            assertEquals(if (team == Team.US) Score(30, 0) else Score(0, 30), score)
            repeat(50) { score = score.change(team, -1) }
            assertEquals(Score(), score)
        }
    }
    @Test fun allThirtyOneScoresHaveCorrectStrokes() {
        for (score in 0..30) {
            val bad = tallyGroups(score, false)
            val good = tallyGroups(score, true)
            assertEquals(score, bad.sum() + good.sum())
            assertEquals(3, bad.size)
            assertEquals(3, good.size)
            assertTrue((bad + good).all { it in 0..5 })
            assertEquals(minOf(15, score), bad.sum())
        }
        assertEquals(listOf(5, 5, 5), tallyGroups(15, false))
        assertEquals(listOf(1, 0, 0), tallyGroups(16, true))
        assertEquals(listOf(5, 5, 5), tallyGroups(30, true))
    }
    @Test fun subtractAcrossPhaseBoundary() {
        assertEquals(Score(15, 8), Score(16, 8).change(Team.US, -1))
    }
    @Test(expected = IllegalArgumentException::class) fun invalidScoreRejected() { Score(31) }
    @Test(expected = IllegalArgumentException::class) fun invalidDeltaRejected() { Score().change(Team.US, 2) }
}
