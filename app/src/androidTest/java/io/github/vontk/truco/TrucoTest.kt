package io.github.vontk.truco

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class TrucoTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val store get() = ScoreStore(InstrumentationRegistry.getInstrumentation().targetContext)
    @Before fun clearScore() {
        runBlocking { store.reset() }
        waitForScore(Team.US, 0)
        waitForScore(Team.THEM, 0)
    }
    private fun zone(team: Team, score: Int, add: Boolean): SemanticsNodeInteraction {
        val label = if (team == Team.US) "Nosotros" else "Ellos"
        return rule.onNode(hasContentDescription("$label: $score puntos") and hasClickLabel(if (add) "Sumar un punto" else "Restar un punto"))
    }
    private fun hasClickLabel(label: String) = SemanticsMatcher("Click: $label") {
        it.config.getOrNull(androidx.compose.ui.semantics.SemanticsActions.OnClick)?.label == label
    }
    private fun waitForScore(team: Team, score: Int) {
        val label = if (team == Team.US) "Nosotros" else "Ellos"
        rule.waitUntil(10_000) {
            rule.onAllNodesWithContentDescription("$label: $score puntos").fetchSemanticsNodes().size == 2
        }
    }
    @Test fun tapsLimitsAndPhases() {
        zone(Team.US, 0, false).performClick()
        waitForScore(Team.US, 0)
        repeat(30) { n -> zone(Team.US, n, true).performClick(); waitForScore(Team.US, n + 1) }
        zone(Team.US, 30, true).performClick()
        waitForScore(Team.US, 30)
        zone(Team.US, 30, false).performClick()
        waitForScore(Team.US, 29)
        waitForScore(Team.THEM, 0)
    }
    @Test fun resetRequiresConfirmationAndSurvivesRecreation() {
        zone(Team.US, 0, true).performClick()
        waitForScore(Team.US, 1)
        rule.onNodeWithContentDescription("Nuevo partido").performClick()
        rule.onNodeWithText("Cancelar").performClick()
        waitForScore(Team.US, 1)
        rule.activityRule.scenario.recreate()
        waitForScore(Team.US, 1)
        rule.onNodeWithContentDescription("Nuevo partido").performClick()
        rule.onNodeWithText("Empezar de nuevo").performClick()
        waitForScore(Team.US, 0)
    }
    @Test fun atomicPersistenceUnderRapidUpdates() = runBlocking {
        repeat(16) { store.change(Team.US, 1) }
        repeat(7) { store.change(Team.THEM, 1) }
        val reopened = ScoreStore(InstrumentationRegistry.getInstrumentation().targetContext)
        assertEquals(Score(16, 7), reopened.scores.first())
        waitForScore(Team.US, 16)
        waitForScore(Team.THEM, 7)
    }
}
