package com.tinnitustracker.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tactile click wrapper. Same contract as [Modifier.clickable] but visually
 * scales the receiver down on press (snappy spring) for a uniform "this is a
 * button" feel across the app. Defaults to no indication ripple so the scale
 * is the dominant feedback; pass an [indication] if a ripple bleed is desired
 * over the press shape (rare).
 *
 * **Place this as the FIRST modifier on a Box/Surface** (i.e. before `.size`,
 * `.shadow`, `.background`, …). The internal `graphicsLayer` must wrap the
 * subsequent draw modifiers so the whole button — chrome and content — scales
 * together on press. Putting it last only scales the children.
 */
fun Modifier.pressableClickable(
    enabled: Boolean = true,
    scaleDown: Float = 0.96f,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 820f),
        label = "pressable-scale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.clickable(
        interactionSource = interaction,
        indication = indication,
        enabled = enabled,
        onClick = onClick
    )
}

/**
 * Hero play control. Layered render:
 *   1. soft colored drop shadow (coral spot, animates with press),
 *   2. circular face with top→bottom coral gradient,
 *   3. drawn-on top gloss (white radial),
 *   4. centered icon,
 *   5. inner light rim for definition.
 *
 * The button shrinks and the shadow tucks in on press, giving the surface a
 * physical "press into the card" feel. Use [variant] to switch the light
 * palette (default, used on cream/light cards) for the dark/studio palette
 * (used over `DarkBg` on the matcher screen).
 */
@Composable
fun PolishedPlayButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
    icon: ImageVector = Icons.Filled.PlayArrow,
    iconSize: Dp = 34.dp,
    variant: PlayButtonVariant = PlayButtonVariant.Coral,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "play-scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed && enabled) 4.dp else variant.restElevation,
        animationSpec = tween(140),
        label = "play-elev"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                spotColor = variant.spotShadow,
                ambientColor = Color.Black
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        variant.gradientTop,
                        variant.gradientMid,
                        variant.gradientBottom
                    )
                )
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription }
            .drawWithContent {
                // Top gloss — drawn before content so the icon sits over it.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0f)
                        ),
                        center = Offset(this.size.width / 2f, this.size.height * 0.18f),
                        radius = this.size.minDimension * 0.55f
                    )
                )
                drawContent()
                // Inner rim — drawn last so it crisply defines the edge.
                drawCircle(
                    color = Color.White.copy(alpha = 0.22f),
                    radius = this.size.minDimension / 2f - 0.8.dp.toPx(),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Color/elevation palette for the [PolishedPlayButton]. */
enum class PlayButtonVariant(
    val gradientTop: Color,
    val gradientMid: Color,
    val gradientBottom: Color,
    val spotShadow: Color,
    val restElevation: Dp
) {
    /** Default — used on cream/light card surfaces (HomeScreen hero). */
    Coral(
        gradientTop    = Color(0xFFE89A7D),
        gradientMid    = com.tinnitustracker.ui.theme.Coral,
        gradientBottom = Color(0xFFAB4B2C),
        spotShadow     = Color(0xFFD67C5C).copy(alpha = 0.85f),
        restElevation  = 16.dp
    ),
    /** Studio dark — used over `DarkBg` (FrequencyMatchingScreen). */
    Orange(
        gradientTop    = Color(0xFFFFA755),
        gradientMid    = OrangeAccent,
        gradientBottom = Color(0xFFD15A00),
        spotShadow     = OrangeAccent.copy(alpha = 0.75f),
        restElevation  = 12.dp
    ),
    /** Secondary CTA — used on coral-soft / teal-soft cards (TfiPromptCard etc.). */
    Teal(
        gradientTop    = Color(0xFF4F8A87),
        gradientMid    = com.tinnitustracker.ui.theme.Teal,
        gradientBottom = Color(0xFF1F4A48),
        spotShadow     = com.tinnitustracker.ui.theme.Teal.copy(alpha = 0.55f),
        restElevation  = 10.dp
    )
}
