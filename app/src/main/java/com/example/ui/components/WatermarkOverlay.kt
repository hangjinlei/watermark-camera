package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WatermarkConfig

/**
 * Interactive Jetpack Compose overlay showing the watermark exactly as in the reference image.
 * Supports tap to edit all fields, long-press to edit remarks.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatermarkOverlay(
    config: WatermarkConfig,
    modifier: Modifier = Modifier,
    currentTime: String = config.getEffectiveTime(),
    showHintBadge: Boolean = true,
    onEditWatermarkClick: () -> Unit = {},
    onEditRemarksClick: () -> Unit = {}
) {
    val scale = config.fontSizeScale
    val baseFontSize = (14.5f * scale).sp
    val lineSpacing = (4 * scale).dp

    val textShadow = Shadow(
        color = Color(0xDD000000),
        offset = Offset(2f, 2f),
        blurRadius = 4f
    )

    val watermarkTextStyle = TextStyle(
        color = Color.White,
        fontSize = baseFontSize,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = FontWeight.Normal,
        shadow = textShadow,
        lineHeight = (20f * scale).sp
    )

    val watermarkLines = config.generateWatermarkLines(currentTime)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Center Watermark: "现场拍照" (Realistic tilted security stamp)
        if (config.showCenterWatermark && config.centerWatermarkText.isNotBlank()) {
            Text(
                text = config.centerWatermarkText,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.20f),
                    fontSize = 38.sp,
                    letterSpacing = 6.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.22f),
                        offset = Offset(2f, 2f),
                        blurRadius = 5f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(-15f)
                    .testTag("center_watermark_text")
            )
        }

        // Bottom Left Watermark Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 24.dp, end = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onEditWatermarkClick,
                    onLongClick = onEditRemarksClick
                )
                .padding(4.dp)
                .testTag("watermark_overlay_container")
        ) {
            // Hint capsule pill badge
            if (showHintBadge && config.showHintBadge) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.48f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "单击编辑水印，长按编辑备注",
                        color = Color.White,
                        fontSize = (11.5f * scale).sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                        fontWeight = FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Watermark text lines (automatically wrapped at 11 characters)
            watermarkLines.forEachIndexed { index, line ->
                Text(text = line, style = watermarkTextStyle)
                if (index < watermarkLines.size - 1) {
                    Spacer(modifier = Modifier.height(lineSpacing))
                }
            }
        }

        // Bottom Right Brand Watermark (Realistic clean camera branding)
        if (config.showBrandWatermark && config.brandWatermarkText.isNotBlank()) {
            Text(
                text = config.brandWatermarkText,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.5.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(1f, 1f),
                        blurRadius = 2.5f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 24.dp)
                    .testTag("brand_watermark_text")
            )
        }
    }
}
