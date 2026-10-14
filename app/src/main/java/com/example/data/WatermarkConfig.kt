package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data model representing the configuration and content of the watermark.
 */
data class WatermarkConfig(
    val longitude: String = "119.819926",
    val latitude: String = "28.908265",
    val coordinateSystem: String = "WGS84坐标系",
    val address: String = "浙江省金华市武义县中源中路52号白洋派出所",
    val time: String = "", // Empty means auto-format current time
    val altitude: String = "0.0米",
    val weather: String = "⛅ 23~26℃ 东风",
    val remarks: String = "长按水印编辑备注",
    
    // Toggles
    val autoGps: Boolean = true,
    val autoAddress: Boolean = true,
    val autoTime: Boolean = true,
    val showCenterWatermark: Boolean = true,
    val centerWatermarkText: String = "现场拍照",
    val showBrandWatermark: Boolean = true,
    val brandWatermarkText: String = "经纬水印相机",
    val showCoordinateSystem: Boolean = true,
    val showAltitude: Boolean = true,
    val showWeather: Boolean = true,
    val showRemarks: Boolean = true,
    val showHintBadge: Boolean = true,
    val fontSizeScale: Float = 1.0f // 0.85 = small, 1.0 = normal, 1.15 = large
) {
    /**
     * Returns the formatted time string to display.
     */
    fun getEffectiveTime(): String {
        return if (autoTime || time.isBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date())
        } else {
            time
        }
    }

    companion object {
        val DEFAULT = WatermarkConfig()
    }
}
