package io.github.vontk.truco

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = isSystemInDarkTheme()
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                )
                if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
            }
            TrucoTheme(dark) {
                val model: ScoreViewModel = viewModel()
                TrucoApp(model)
            }
        }
    }
}

@Composable
internal fun TrucoTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= 31 && dark -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= 31 -> dynamicLightColorScheme(context)
        dark -> darkColorScheme(
            primary = Color(0xFFD0D0D0), onPrimary = Color(0xFF282828),
            background = Color(0xFF121212), onBackground = Color(0xFFEEEEEE),
            surface = Color(0xFF121212), onSurface = Color(0xFFEEEEEE),
            surfaceContainerHigh = Color(0xFF292929),
            onSurfaceVariant = Color(0xFFCACACA), outlineVariant = Color(0xFF454545)
        )
        else -> lightColorScheme(
            primary = Color(0xFF555555), onPrimary = Color.White,
            background = Color(0xFFF4F4F4), onBackground = Color(0xFF1C1C1C),
            surface = Color(0xFFF4F4F4), onSurface = Color(0xFF1C1C1C),
            surfaceContainerHigh = Color(0xFFE8E8E8),
            onSurfaceVariant = Color(0xFF494949), outlineVariant = Color(0xFFC5C5C5)
        )
    }
    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(LocalContentColor provides colors.onBackground, content = content)
    }
}
