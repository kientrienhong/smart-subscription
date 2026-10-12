package com.timeskip.smartsubscription

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.timeskip.smartsubscription.ui.screens.AddScreen
import com.timeskip.smartsubscription.ui.screens.AnalyticsScreen
import com.timeskip.smartsubscription.ui.screens.ArchiveScreen
import com.timeskip.smartsubscription.ui.screens.CategoriesScreen
import com.timeskip.smartsubscription.ui.screens.DetailScreen
import com.timeskip.smartsubscription.ui.screens.FormScreen
import com.timeskip.smartsubscription.ui.screens.HomeScreen
import com.timeskip.smartsubscription.ui.screens.ScanScreen
import com.timeskip.smartsubscription.ui.screens.SettingsScreen
import com.timeskip.smartsubscription.ui.screens.UnlockScreen
import com.timeskip.smartsubscription.ui.theme.LocalSs
import com.timeskip.smartsubscription.ui.theme.SmartSubscriptionTheme
import com.timeskip.smartsubscription.ui.theme.SsType
import com.timeskip.smartsubscription.widget.UpcomingWidget

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Every screen opens with the navy ink block, so status bar icons are always light.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()))
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(state.subscriptions, state.categories) { UpcomingWidget.publish(applicationContext, state) }
            SmartSubscriptionTheme(state.theme) {
                App(viewModel)
            }
        }
    }
}

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val SCAN = "scan/{source}"
    const val FORM = "form?edit={edit}&scanned={scanned}"
    const val DETAIL = "detail/{id}"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val CATEGORIES = "categories"
    const val ARCHIVE = "archive"
    const val UNLOCK = "unlock"

    fun scan(source: String) = "scan/$source"
    fun form(edit: String? = null, scanned: Boolean = false) = "form?edit=${edit.orEmpty()}&scanned=$scanned"
    fun detail(id: String) = "detail/$id"
}

@Composable
private fun App(vm: AppViewModel) {
    val ss = LocalSs.current
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.messages.collect { snackbar.showSnackbar(it) }
    }

    Box(Modifier.fillMaxSize().background(ss.bg)) {
        NavHost(navController = nav, startDestination = Routes.HOME) {
            composable(Routes.HOME) { HomeScreen(vm, nav) }
            composable(Routes.ADD) { AddScreen(vm, nav) }
            composable(Routes.SCAN, listOf(navArgument("source") { type = NavType.StringType })) {
                ScanScreen(it.arguments?.getString("source") ?: "camera", nav, vm)
            }
            composable(
                Routes.FORM,
                listOf(
                    navArgument("edit") { type = NavType.StringType; defaultValue = "" },
                    navArgument("scanned") { type = NavType.BoolType; defaultValue = false },
                ),
            ) {
                FormScreen(vm, nav, it.arguments?.getString("edit")?.ifEmpty { null }, it.arguments?.getBoolean("scanned") ?: false)
            }
            composable(Routes.DETAIL, listOf(navArgument("id") { type = NavType.StringType })) {
                DetailScreen(vm, nav, it.arguments?.getString("id").orEmpty())
            }
            composable(Routes.ANALYTICS) { AnalyticsScreen(vm, nav) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, nav) }
            composable(Routes.CATEGORIES) { CategoriesScreen(vm, nav) }
            composable(Routes.ARCHIVE) { ArchiveScreen(vm, nav) }
            composable(Routes.UNLOCK) { UnlockScreen(vm, nav) }
        }
        SnackbarHost(
            snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 88.dp),
        ) { data ->
            Snackbar(shape = RoundedCornerShape(10.dp), containerColor = ss.ink, contentColor = ss.onInk) {
                Text(data.visuals.message, style = SsType.sub)
            }
        }
    }
}
