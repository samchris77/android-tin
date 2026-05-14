package com.tinnitustracker.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Single source of truth for radii, spacing, and elevation across the app.
 * Use these instead of raw `.dp` literals. Adding a new variant means adding
 * a constant here, not scattering a new value into a screen file.
 *
 * Canonical values locked 2026-05-14 — see `wiki/app/ux-principles.md §5`.
 */
object Radius {
    val card   = 16.dp
    val chip   = 8.dp
    val button = 12.dp
    val pill   = 100.dp
}

object Spacing {
    val xs      = 4.dp
    val sm      = 8.dp
    val md      = 12.dp
    val lg      = 16.dp
    val xl      = 20.dp
    val xxl     = 24.dp
    val section = 32.dp
}

object Elevation {
    val card   = 2.dp
    val raised = 4.dp
    val hero   = 8.dp
}
