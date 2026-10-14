package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.WatermarkConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full editor dialog for customizing all watermark parameters.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditWatermarkDialog(
    currentConfig: WatermarkConfig,
    onDismiss: () -> Unit,
    onFetchCurrentGps: () -> Unit,
    onConfirm: (newConfig: WatermarkConfig, saveAsDefault: Boolean) -> Unit
) {
    var longitude by remember { mutableStateOf(currentConfig.longitude) }
    var latitude by remember { mutableStateOf(currentConfig.latitude) }
    var coordinateSystem by remember { mutableStateOf(currentConfig.coordinateSystem) }
    var address by remember { mutableStateOf(currentConfig.address) }
    var time by remember { mutableStateOf(currentConfig.time.ifBlank { currentConfig.getEffectiveTime() }) }
    var altitude by remember { mutableStateOf(currentConfig.altitude) }
    var weather by remember { mutableStateOf(currentConfig.weather) }
    var remarks by remember { mutableStateOf(currentConfig.remarks) }
    var saveAsDefault by remember { mutableStateOf(false) }

    val weatherPresets = listOf(
        "⛅ 23~26℃ 东风",
        "☀️ 28℃ 晴",
        "⛅ 25℃ 多云",
        "🌧️ 20℃ 小雨",
        "⛈️ 22℃ 雷阵雨",
        "💨 21℃ 东北风",
        "❄️ 1℃ 小雪"
    )

    val coordPresets = listOf("WGS84坐标系", "GCJ-02坐标系", "BD-09坐标系")

    val remarksPresets = listOf(
        "现场拍照存证",
        "工程施工记录",
        "日常巡查打卡",
        "安全隐患整改",
        "勘察测绘记录"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "自定义水印内容",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GPS & Location Quick Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "经纬度与坐标",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedButton(
                        onClick = onFetchCurrentGps,
                        modifier = Modifier.testTag("refresh_gps_button")
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("获取实时GPS", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("经度") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_longitude"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("纬度") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_latitude"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Coordinate system selection chips
                Text(text = "坐标系选择", style = MaterialTheme.typography.bodySmall)
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

                Spacer(modifier = Modifier.height(14.dp))

                // Address field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("地址 (支持多行自动换行)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_address"),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Time field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("时间 (yyyy-MM-dd HH:mm:ss)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_time"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            time = sdf.format(Date())
                        }
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("当前时间", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Altitude
                OutlinedTextField(
                    value = altitude,
                    onValueChange = { altitude = it },
                    label = { Text("海拔") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_altitude"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Weather field and presets
                OutlinedTextField(
                    value = weather,
                    onValueChange = { weather = it },
                    label = { Text("天气状况") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_weather"),
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

                Spacer(modifier = Modifier.height(14.dp))

                // Remarks field and suggestions
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("备注信息") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_remarks"),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    remarksPresets.forEach { preset ->
                        FilterChip(
                            selected = remarks == preset,
                            onClick = { remarks = preset },
                            label = { Text(preset, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Save as default" checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { saveAsDefault = !saveAsDefault }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveAsDefault,
                        onCheckedChange = { saveAsDefault = it },
                        modifier = Modifier.testTag("checkbox_save_default")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "保存为默认值",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "下次打开程序无需重复设置这些字段",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updatedConfig = currentConfig.copy(
                                longitude = longitude,
                                latitude = latitude,
                                coordinateSystem = coordinateSystem,
                                address = address,
                                time = time,
                                altitude = altitude,
                                weather = weather,
                                remarks = remarks
                            )
                            onConfirm(updatedConfig, saveAsDefault)
                        },
                        modifier = Modifier.testTag("save_watermark_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("应用水印")
                    }
                }
            }
        }
    }
}
