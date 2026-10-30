package io.github.vontk.truco

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
internal fun TrucoApp(model: ScoreViewModel) {
    val score by model.score.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val error = stringResource(R.string.save_error)
    LaunchedEffect(model, lifecycle, error) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            model.saveErrors.collect { snackbar.showSnackbar(error) }
        }
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        score?.let { TrucoScreen(it, model::change, model::reset) }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).safeDrawingPadding())
    }
}

@Composable
internal fun TrucoScreen(score: Score, change: (Team, Int) -> Unit, reset: () -> Unit) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val divider = MaterialTheme.colorScheme.outlineVariant
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .height(64.dp).padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_tally), null, Modifier.size(26.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.brand), fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { confirm = true }, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_reset), stringResource(R.string.new_game))
            }
        }
        // The cross fills the window width and continues behind the real navigation bar.
        Column(Modifier.weight(1f).fillMaxWidth().drawBehind {
            drawLine(divider, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx())
            drawLine(divider, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx())
        }) {
            for (good in listOf(false, true)) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    for (team in Team.entries) {
                        val points = if (team == Team.US) score.us else score.them
                        val delta = if (good) 1 else -1
                        Zone(team, points, good, Modifier.weight(1f).fillMaxHeight()) {
                            if ((points + delta) in 0..30) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                change(team, delta)
                            }
                        }
                    }
                }
            }
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text(stringResource(R.string.reset_title)) },
        text = { Text(stringResource(R.string.reset_message)) },
        confirmButton = {
            TextButton(onClick = { reset(); confirm = false }) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun Zone(team: Team, score: Int, good: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val name = stringResource(if (team == Team.US) R.string.us else R.string.them)
    val action = stringResource(if (good) R.string.add else R.string.subtract)
    val description = stringResource(R.string.team_points, name, score)
    val points = stringResource(R.string.points, score)
    BoxWithConstraints(
        modifier.clickable(onClickLabel = action, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                stateDescription = points
            }
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .then(if (good) Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)) else Modifier)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        val compact = maxHeight < 210.dp
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!good) {
                if (compact) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clearAndSetSemantics {})
                    Text("$score", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clearAndSetSemantics {})
                } else {
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clearAndSetSemantics {})
                    Text("$score", fontSize = 56.sp, lineHeight = 60.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = (-2).sp, modifier = Modifier.clearAndSetSemantics {})
                }
            }
            Spacer(Modifier.height(if (good && !compact) 20.dp else 8.dp))
            Tallies(tallyGroups(score, good), compact, Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
private fun Tallies(groups: List<Int>, horizontal: Boolean, modifier: Modifier) {
    val ink = MaterialTheme.colorScheme.onSurfaceVariant
    val ghost = ink.copy(alpha = 0.13f)
    Canvas(modifier.clearAndSetSemantics {}) {
        val gap = 8.dp.toPx()
        val side = min(68.dp.toPx(), if (horizontal) min((size.width - 2 * gap) / 3, size.height)
            else min(size.width, (size.height - 2 * gap) / 3)).coerceAtLeast(0f)
        val startX = if (horizontal) (size.width - side * 3 - gap * 2) / 2 else (size.width - side) / 2
        val strokes = arrayOf(
            floatArrayOf(13f, 12f, 51f, 12f), floatArrayOf(51f, 12f, 51f, 50f),
            floatArrayOf(51f, 50f, 13f, 50f), floatArrayOf(13f, 50f, 13f, 12f),
            floatArrayOf(13f, 50f, 51f, 12f)
        )
        groups.forEachIndexed { i, count ->
            val x = startX + if (horizontal) i * (side + gap) else 0f
            val y = if (horizontal) 0f else i * (side + gap)
            strokes.forEachIndexed { index, p ->
                drawLine(if (index < count) ink else ghost,
                    Offset(x + p[0] * side / 64, y + p[1] * side / 64),
                    Offset(x + p[2] * side / 64, y + p[3] * side / 64),
                    5.4f * side / 64, StrokeCap.Round)
            }
        }
    }
}
