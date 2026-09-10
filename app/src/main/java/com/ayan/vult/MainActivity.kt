package com.ayan.vult

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ayan.vult.data.AppDatabase
import com.ayan.vult.data.AppRepository
import com.ayan.vult.navigation.VultNavKey
import com.ayan.vult.ui.dashboard.DashboardScreen
import com.ayan.vult.ui.dashboard.DashboardViewModel
import com.ayan.vult.ui.settings.SettingsScreen
import com.ayan.vult.ui.theme.VultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VultTheme {
                VultApp()
            }
        }
    }
}

@Composable
fun VultApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { AppRepository(context, database.blockedAppDao()) }
    
    val backStack = remember { mutableStateListOf<NavKey>(VultNavKey.Dashboard) }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<VultNavKey.Dashboard> {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return DashboardViewModel(repository) as T
                        }
                    }
                )
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToSettings = { backStack.add(VultNavKey.Settings) }
                )
            }
            entry<VultNavKey.Settings> {
                SettingsScreen(
                    onBack = { backStack.removeAt(backStack.size - 1) }
                )
            }
        }
    )
}
