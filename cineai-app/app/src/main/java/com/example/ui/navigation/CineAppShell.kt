package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.ui.screens.camera.CameraScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.editor.EditorScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.presets.PresetsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.shorts.ShortsScreen
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineGoldPrimary
import com.example.ui.theme.CineObsidian
import com.example.ui.theme.CinePillIndicator
import com.example.ui.theme.CineRedRecord
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTealAccent
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.viewmodel.StudioViewModel

data class NavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val navigationItems = listOf(
    NavItem(Screen.Dashboard, Icons.Default.Home, "Studio"),
    NavItem(Screen.Camera, Icons.Default.CameraAlt, "AI Camera"),
    NavItem(Screen.Library, Icons.Default.Collections, "Media Vault"),
    NavItem(Screen.Presets, Icons.Default.Palette, "LUT Styles")
)

@Composable
fun CineAppShell(
    viewModel: StudioViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val snackMessage by viewModel.snackMessage.collectAsState()

    LaunchedEffect(snackMessage) {
        snackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnack()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CineObsidian)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Tablet / Desktop layout with Sidebar Navigation Rail.
            // Wrap in a Scaffold so snackbar feedback is also shown on wide screens.
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NavigationRail(
                    containerColor = CineSurface,
                    contentColor = Color.White,
                    header = {
                        Surface(
                            color = CineGoldPrimary,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "CINE",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    navigationItems.forEach { item ->
                        val isSelected = currentRoute == item.screen.route
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = CineGoldPrimary,
                                selectedTextColor = CineGoldPrimary,
                                indicatorColor = CinePillIndicator,
                                unselectedIconColor = CineTextSecondary,
                                unselectedTextColor = CineTextSecondary
                            )
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    CineNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
                }
            }
        } else {
            // Mobile layout with Bottom Navigation Bar
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    val hideBottomBarOnEditor = currentRoute?.startsWith("editor/") == true
                    if (!hideBottomBarOnEditor) {
                        NavigationBar(
                            containerColor = CineSurface,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                        ) {
                            navigationItems.forEach { item ->
                                val isSelected = currentRoute == item.screen.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CineGoldPrimary,
                                        selectedTextColor = CineGoldPrimary,
                                        indicatorColor = CinePillIndicator,
                                        unselectedIconColor = CineTextSecondary,
                                        unselectedTextColor = CineTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    CineNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CineNavHost(
    navController: NavHostController,
    viewModel: StudioViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToEditor = { mediaId -> navController.navigate(Screen.Editor.createRoute(mediaId)) },
                onNavigateToShorts = { navController.navigate(Screen.Shorts.route) },
                onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                onNavigateToPresets = { navController.navigate(Screen.Presets.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                viewModel = viewModel,
                onNavigateToEditor = { mediaId -> navController.navigate(Screen.Editor.createRoute(mediaId)) },
                onNavigateToLibrary = { navController.navigate(Screen.Library.route) }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId") ?: ""
            EditorScreen(
                mediaId = mediaId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Shorts.route) {
            ShortsScreen(
                viewModel = viewModel,
                onNavigateToEditor = { mediaId -> navController.navigate(Screen.Editor.createRoute(mediaId)) }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                viewModel = viewModel,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToEditor = { mediaId -> navController.navigate(Screen.Editor.createRoute(mediaId)) }
            )
        }

        composable(Screen.Presets.route) {
            PresetsScreen(
                viewModel = viewModel,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel
            )
        }
    }
}
