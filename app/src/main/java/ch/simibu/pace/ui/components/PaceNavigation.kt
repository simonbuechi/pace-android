package ch.simibu.pace.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.simibu.pace.R

enum class PaceScreen(val route: String) {
    QUICK_START("quick_start"),
    ROUTINES("routines"),
    SETTINGS("settings"),
    ACTIVE_TIMER("active_timer")
}

@Composable
fun PaceBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == PaceScreen.QUICK_START.route,
            onClick = { onNavigate(PaceScreen.QUICK_START.route) },
            icon = { Icon(Icons.AutoMirrored.Rounded.DirectionsRun, contentDescription = stringResource(R.string.nav_quick_start)) },
            label = { Text(stringResource(R.string.nav_quick_start)) }
        )
        NavigationBarItem(
            selected = currentRoute == PaceScreen.ROUTINES.route,
            onClick = { onNavigate(PaceScreen.ROUTINES.route) },
            icon = { Icon(Icons.AutoMirrored.Rounded.List, contentDescription = stringResource(R.string.nav_routines)) },
            label = { Text(stringResource(R.string.nav_routines)) }
        )
        NavigationBarItem(
            selected = currentRoute == PaceScreen.SETTINGS.route,
            onClick = { onNavigate(PaceScreen.SETTINGS.route) },
            icon = { Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.nav_settings)) },
            label = { Text(stringResource(R.string.nav_settings)) }
        )
    }
}
