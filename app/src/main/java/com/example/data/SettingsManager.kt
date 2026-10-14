package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * Manages persistent storage of default watermark configuration and captured photos.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("watermark_camera_prefs", Context.MODE_PRIVATE)

    fun getDefaultConfig(): WatermarkConfig {
        return WatermarkConfig(
            longitude = prefs.getString(KEY_LONGITUDE, "119.819926") ?: "119.819926",
            latitude = prefs.getString(KEY_LATITUDE, "28.908265") ?: "28.908265",
            coordinateSystem = prefs.getString(KEY_COORD_SYS, "WGS84坐标系") ?: "WGS84坐标系",
            address = prefs.getString(KEY_ADDRESS, "浙江省金华市武义县中源中路52号白洋派出所")
                ?: "浙江省金华市武义县中源中路52号白洋派出所",
            time = prefs.getString(KEY_TIME, "") ?: "",
            altitude = prefs.getString(KEY_ALTITUDE, "0.0米") ?: "0.0米",
            weather = prefs.getString(KEY_WEATHER, "⛅ 23~26℃ 东风") ?: "⛅ 23~26℃ 东风",
            remarks = prefs.getString(KEY_REMARKS, "长按水印编辑备注") ?: "长按水印编辑备注",
            autoGps = prefs.getBoolean(KEY_AUTO_GPS, true),
            autoAddress = prefs.getBoolean(KEY_AUTO_ADDRESS, true),
            autoTime = prefs.getBoolean(KEY_AUTO_TIME, true),
            showCenterWatermark = prefs.getBoolean(KEY_SHOW_CENTER, true),
            centerWatermarkText = prefs.getString(KEY_CENTER_TEXT, "现场拍照") ?: "现场拍照",
            showBrandWatermark = prefs.getBoolean(KEY_SHOW_BRAND, true),
            brandWatermarkText = prefs.getString(KEY_BRAND_TEXT, "经纬水印相机") ?: "经纬水印相机",
            showCoordinateSystem = prefs.getBoolean(KEY_SHOW_COORD_SYS, true),
            showAltitude = prefs.getBoolean(KEY_SHOW_ALTITUDE, true),
            showWeather = prefs.getBoolean(KEY_SHOW_WEATHER, true),
            showRemarks = prefs.getBoolean(KEY_SHOW_REMARKS, true),
            showHintBadge = prefs.getBoolean(KEY_SHOW_HINT_BADGE, true),
            fontSizeScale = prefs.getFloat(KEY_FONT_SCALE, 1.0f)
        )
    }

    fun saveDefaultConfig(config: WatermarkConfig) {
        prefs.edit().apply {
            putString(KEY_LONGITUDE, config.longitude)
            putString(KEY_LATITUDE, config.latitude)
            putString(KEY_COORD_SYS, config.coordinateSystem)
            putString(KEY_ADDRESS, config.address)
            putString(KEY_TIME, config.time)
            putString(KEY_ALTITUDE, config.altitude)
            putString(KEY_WEATHER, config.weather)
            putString(KEY_REMARKS, config.remarks)
            putBoolean(KEY_AUTO_GPS, config.autoGps)
            putBoolean(KEY_AUTO_ADDRESS, config.autoAddress)
            putBoolean(KEY_AUTO_TIME, config.autoTime)
            putBoolean(KEY_SHOW_CENTER, config.showCenterWatermark)
            putString(KEY_CENTER_TEXT, config.centerWatermarkText)
            putBoolean(KEY_SHOW_BRAND, config.showBrandWatermark)
            putString(KEY_BRAND_TEXT, config.brandWatermarkText)
            putBoolean(KEY_SHOW_COORD_SYS, config.showCoordinateSystem)
            putBoolean(KEY_SHOW_ALTITUDE, config.showAltitude)
            putBoolean(KEY_SHOW_WEATHER, config.showWeather)
            putBoolean(KEY_SHOW_REMARKS, config.showRemarks)
            putBoolean(KEY_SHOW_HINT_BADGE, config.showHintBadge)
            putFloat(KEY_FONT_SCALE, config.fontSizeScale)
            apply()
        }
    }

    fun resetToDefaults(): WatermarkConfig {
        val defaultConfig = WatermarkConfig.DEFAULT
        saveDefaultConfig(defaultConfig)
        return defaultConfig
    }

    companion object {
        private const val KEY_LONGITUDE = "key_longitude"
        private const val KEY_LATITUDE = "key_latitude"
        private const val KEY_COORD_SYS = "key_coord_sys"
        private const val KEY_ADDRESS = "key_address"
        private const val KEY_TIME = "key_time"
        private const val KEY_ALTITUDE = "key_altitude"
        private const val KEY_WEATHER = "key_weather"
        private const val KEY_REMARKS = "key_remarks"
        private const val KEY_AUTO_GPS = "key_auto_gps"
        private const val KEY_AUTO_ADDRESS = "key_auto_address"
        private const val KEY_AUTO_TIME = "key_auto_time"
        private const val KEY_SHOW_CENTER = "key_show_center"
        private const val KEY_CENTER_TEXT = "key_center_text"
        private const val KEY_SHOW_BRAND = "key_show_brand"
        private const val KEY_BRAND_TEXT = "key_brand_text"
        private const val KEY_SHOW_COORD_SYS = "key_show_coord_sys"
        private const val KEY_SHOW_ALTITUDE = "key_show_altitude"
        private const val KEY_SHOW_WEATHER = "key_show_weather"
        private const val KEY_SHOW_REMARKS = "key_show_remarks"
        private const val KEY_SHOW_HINT_BADGE = "key_show_hint_badge"
        private const val KEY_FONT_SCALE = "key_font_scale"
    }
}
