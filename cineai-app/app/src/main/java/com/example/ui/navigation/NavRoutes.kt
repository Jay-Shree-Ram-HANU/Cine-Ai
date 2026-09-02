package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Studio")
    object Camera : Screen("camera", "AI Camera")
    object Editor : Screen("editor/{mediaId}", "AI Colorist") {
        fun createRoute(mediaId: String) = "editor/$mediaId"
    }
    object Shorts : Screen("shorts", "Shorts 24fps")
    object Library : Screen("library", "Media Vault")
    object Presets : Screen("presets", "LUT Presets")
    object Profile : Screen("profile", "Director")
}
