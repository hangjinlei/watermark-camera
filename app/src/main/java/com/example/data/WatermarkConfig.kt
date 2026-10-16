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
     * When autoTime is false and a custom time is set, returns the custom time.
     */
    fun getEffectiveTime(): String {
        return if (!autoTime && time.isNotBlank()) {
            time
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date())
        }
    }

    /**
     * Generates all watermark lines in order.
     * If any field's content exceeds 11 characters (e.g. address or remarks),
     * it is wrapped with at most 11 characters per line.
     */
    fun generateWatermarkLines(effectiveTime: String = getEffectiveTime()): List<String> {
        val result = mutableListOf<String>()

        fun addWrappedField(prefix: String, content: String, maxChars: Int = 11) {
            val cleanContent = content.trim()
            if (cleanContent.isEmpty()) return
            if (cleanContent.length <= maxChars) {
                result.add("$prefix: $cleanContent")
            } else {
                result.add("$prefix: ${cleanContent.substring(0, maxChars)}")
                var remainder = cleanContent.substring(maxChars)
                while (remainder.isNotEmpty()) {
                    val takeLen = minOf(maxChars, remainder.length)
                    result.add(remainder.substring(0, takeLen))
                    remainder = remainder.substring(takeLen)
                }
            }
        }

        // 1. 经度
        addWrappedField("经度", longitude)
        // 2. 纬度
        addWrappedField("纬度", latitude)
        // 3. 坐标系
        if (showCoordinateSystem && coordinateSystem.isNotBlank()) {
            addWrappedField("坐标系", coordinateSystem)
        }
        // 4. 地址 (超过11个字符换行)
        addWrappedField("地址", address, 11)
        // 5. 时间 (标准时间单行展示，若超长亦换行)
        if (effectiveTime.length <= 25) {
            result.add("时间: $effectiveTime")
        } else {
            addWrappedField("时间", effectiveTime, 11)
        }
        // 6. 海拔
        if (showAltitude && altitude.isNotBlank()) {
            addWrappedField("海拔", altitude)
        }
        // 7. 天气 (超过11个字符换行)
        if (showWeather && weather.isNotBlank()) {
            addWrappedField("天气", weather, 11)
        }
        // 8. 备注 (超过11个字符换行)
        if (showRemarks && remarks.isNotBlank()) {
            addWrappedField("备注", remarks, 11)
        }

        return result
    }

    companion object {
        val DEFAULT = WatermarkConfig()
    }
}
