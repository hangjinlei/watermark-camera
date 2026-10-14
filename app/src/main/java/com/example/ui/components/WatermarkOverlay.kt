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
        fontWeight = FontWeight.SemiBold,
        shadow = textShadow,
        lineHeight = (20f * scale).sp
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Center Watermark: "现场拍照"
        if (config.showCenterWatermark && config.centerWatermarkText.isNotBlank()) {
            Text(
                text = config.centerWatermarkText,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.22f),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(2f, 2f),
                        blurRadius = 6f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.Center)
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
                        fontWeight = FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Watermark text lines
            Text(text = "经度: ${config.longitude}", style = watermarkTextStyle)
            Spacer(modifier = Modifier.height(lineSpacing))

            Text(text = "纬度: ${config.latitude}", style = watermarkTextStyle)
            Spacer(modifier = Modifier.height(lineSpacing))

            if (config.showCoordinateSystem && config.coordinateSystem.isNotBlank()) {
                Text(text = "坐标系: ${config.coordinateSystem}", style = watermarkTextStyle)
                Spacer(modifier = Modifier.height(lineSpacing))
            }

            Text(
                text = "地址: ${config.address}",
                style = watermarkTextStyle,
                modifier = Modifier.widthIn(max = 320.dp)
            )
            Spacer(modifier = Modifier.height(lineSpacing))

            Text(text = "时间: $currentTime", style = watermarkTextStyle)
            Spacer(modifier = Modifier.height(lineSpacing))

            if (config.showAltitude && config.altitude.isNotBlank()) {
                Text(text = "海拔: ${config.altitude}", style = watermarkTextStyle)
                Spacer(modifier = Modifier.height(lineSpacing))
            }

            if (config.showWeather && config.weather.isNotBlank()) {
                Text(text = "天气: ${config.weather}", style = watermarkTextStyle)
                Spacer(modifier = Modifier.height(lineSpacing))
            }

            if (config.showRemarks && config.remarks.isNotBlank()) {
                Text(text = "备注: ${config.remarks}", style = watermarkTextStyle)
            }
        }

        // Bottom Right Brand Watermark
        if (config.showBrandWatermark && config.brandWatermarkText.isNotBlank()) {
            Text(
                text = config.brandWatermarkText,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        offset = Offset(1f, 1f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 22.dp)
                    .testTag("brand_watermark_text")
            )
        }
    }
}
