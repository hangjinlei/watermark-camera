package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.data.WatermarkConfig

/**
 * Dedicated settings screen for permanently configuring default watermark values.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentDefault by remember { mutableStateOf(settingsManager.getDefaultConfig()) }

    var longitude by remember { mutableStateOf(currentDefault.longitude) }
    var latitude by remember { mutableStateOf(currentDefault.latitude) }
    var coordinateSystem by remember { mutableStateOf(currentDefault.coordinateSystem) }
    var address by remember { mutableStateOf(currentDefault.address) }
    var altitude by remember { mutableStateOf(currentDefault.altitude) }
    var weather by remember { mutableStateOf(currentDefault.weather) }
    var remarks by remember { mutableStateOf(currentDefault.remarks) }

    var autoGps by remember { mutableStateOf(currentDefault.autoGps) }
    var autoAddress by remember { mutableStateOf(currentDefault.autoAddress) }
    var autoTime by remember { mutableStateOf(currentDefault.autoTime) }

    var showCenterWatermark by remember { mutableStateOf(currentDefault.showCenterWatermark) }
    var centerWatermarkText by remember { mutableStateOf(currentDefault.centerWatermarkText) }
    var showBrandWatermark by remember { mutableStateOf(currentDefault.showBrandWatermark) }
    var brandWatermarkText by remember { mutableStateOf(currentDefault.brandWatermarkText) }
    var showCoordinateSystem by remember { mutableStateOf(currentDefault.showCoordinateSystem) }
    var showAltitude by remember { mutableStateOf(currentDefault.showAltitude) }
    var showWeather by remember { mutableStateOf(currentDefault.showWeather) }
    var showRemarks by remember { mutableStateOf(currentDefault.showRemarks) }
    var showHintBadge by remember { mutableStateOf(currentDefault.showHintBadge) }
    var fontSizeScale by remember { mutableFloatStateOf(currentDefault.fontSizeScale) }

    val weatherPresets = listOf(
        "⛅ 23~26℃ 东风",
        "☀️ 28℃ 晴",
        "⛅ 25℃ 多云",
        "🌧️ 20℃ 小雨",
        "⛈️ 22℃ 雷阵雨",
        "💨 21℃ 东北风"
    )

    val coordPresets = listOf("WGS84坐标系", "GCJ-02坐标系", "BD-09坐标系")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("水印默认值设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val reset = settingsManager.resetToDefaults()
                            longitude = reset.longitude
                            latitude = reset.latitude
                            coordinateSystem = reset.coordinateSystem
                            address = reset.address
                            altitude = reset.altitude
                            weather = reset.weather
                            remarks = reset.remarks
                            autoGps = reset.autoGps
                            autoAddress = reset.autoAddress
                            autoTime = reset.autoTime
                            showCenterWatermark = reset.showCenterWatermark
                            centerWatermarkText = reset.centerWatermarkText
                            showBrandWatermark = reset.showBrandWatermark
                            brandWatermarkText = reset.brandWatermarkText
                            showCoordinateSystem = reset.showCoordinateSystem
                            showAltitude = reset.showAltitude
                            showWeather = reset.showWeather
                            showRemarks = reset.showRemarks
                            showHintBadge = reset.showHintBadge
                            fontSizeScale = reset.fontSizeScale
                            Toast.makeText(context, "已恢复出厂默认值", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("reset_defaults_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "恢复默认")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("返回相机")
                    }
                    Button(
                        onClick = {
                            val newConfig = WatermarkConfig(
                                longitude = longitude,
                                latitude = latitude,
                                coordinateSystem = coordinateSystem,
                                address = address,
                                time = "",
                                altitude = altitude,
                                weather = weather,
                                remarks = remarks,
                                autoGps = autoGps,
                                autoAddress = autoAddress,
                                autoTime = autoTime,
                                showCenterWatermark = showCenterWatermark,
                                centerWatermarkText = centerWatermarkText,
                                showBrandWatermark = showBrandWatermark,
                                brandWatermarkText = brandWatermarkText,
                                showCoordinateSystem = showCoordinateSystem,
                                showAltitude = showAltitude,
                                showWeather = showWeather,
                                showRemarks = showRemarks,
                                showHintBadge = showHintBadge,
                                fontSizeScale = fontSizeScale
                            )
                            settingsManager.saveDefaultConfig(newConfig)
                            Toast.makeText(context, "默认值已成功保存！下次启动自动生效", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_settings_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("保存默认值")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Group 1: 默认水印字段内容
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "固定默认内容配置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "设置好后，每次拍照将默认直接使用这套内容，无需每次手动重新输入",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // 经纬度
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("默认经度") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("默认纬度") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 坐标系
                    Text(text = "默认坐标系", style = MaterialTheme.typography.bodySmall)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        coordPresets.forEach { sys ->
                            FilterChip(
                                selected = coordinateSystem == sys,
                                onClick = { coordinateSystem = sys },
                                label = { Text(sys, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 地址
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("默认详细地址") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 海拔
                    OutlinedTextField(
                        value = altitude,
                        onValueChange = { altitude = it },
                        label = { Text("默认海拔高度 (如 0.0米)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 天气
                    OutlinedTextField(
                        value = weather,
                        onValueChange = { weather = it },
                        label = { Text("默认天气") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weatherPresets.forEach { preset ->
                            FilterChip(
                                selected = weather == preset,
                                onClick = { weather = preset },
                                label = { Text(preset, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 备注
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("默认备注信息") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 2: 定位与自动化更新
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "自动化定位与更新",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "自动获取实时GPS位置", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "开启后有GPS信号时自动填写真实坐标，无信号时使用上述默认值",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoGps,
                            onCheckedChange = { autoGps = it },
                            modifier = Modifier.testTag("switch_auto_gps")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "自动逆地理编码街道地址", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "开启后根据当前位置自动获取省市区街道，获取失败时使用默认地址",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoAddress,
                            onCheckedChange = { autoAddress = it },
                            modifier = Modifier.testTag("switch_auto_address")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "实时时间走动", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "开启后水印时间随真实北京时间每秒走动更新",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoTime,
                            onCheckedChange = { autoTime = it },
                            modifier = Modifier.testTag("switch_auto_time")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 3: 显示开关与视觉样式
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DisplaySettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "水印显示与版式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 现场拍照防伪水印
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "画面中心防伪水印", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "在画面中央显示半透明背景字（如“现场拍照”）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showCenterWatermark,
                            onCheckedChange = { showCenterWatermark = it }
                        )
                    }

                    if (showCenterWatermark) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = centerWatermarkText,
                            onValueChange = { centerWatermarkText = it },
                            label = { Text("中心水印文字") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 品牌水印
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "右下角标识水印", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "在画面右下角显示相机名称",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showBrandWatermark,
                            onCheckedChange = { showBrandWatermark = it }
                        )
                    }

                    if (showBrandWatermark) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = brandWatermarkText,
                            onValueChange = { brandWatermarkText = it },
                            label = { Text("右下角水印文字") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 字段显示项
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "显示坐标系字段", fontWeight = FontWeight.SemiBold)
                        Switch(checked = showCoordinateSystem, onCheckedChange = { showCoordinateSystem = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "显示海拔字段", fontWeight = FontWeight.SemiBold)
                        Switch(checked = showAltitude, onCheckedChange = { showAltitude = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "显示天气字段", fontWeight = FontWeight.SemiBold)
                        Switch(checked = showWeather, onCheckedChange = { showWeather = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "显示备注字段", fontWeight = FontWeight.SemiBold)
                        Switch(checked = showRemarks, onCheckedChange = { showRemarks = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "显示“单击编辑，长按备注”胶囊条", fontWeight = FontWeight.SemiBold)
                        Switch(checked = showHintBadge, onCheckedChange = { showHintBadge = it })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 字体大小缩放
                    Text(
                        text = "水印字体大小缩放: ${(fontSizeScale * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = fontSizeScale,
                        onValueChange = { fontSizeScale = it },
                        valueRange = 0.8f..1.4f,
                        steps = 5,
                        modifier = Modifier.testTag("slider_font_size")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
