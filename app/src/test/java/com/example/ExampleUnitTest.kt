package com.example

import com.example.data.WatermarkConfig
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAddressAndRemarksWrappingAt11Characters() {
    val config = WatermarkConfig(
      address = "浙江省金华市武义县中源中路52号白洋派出所",
      remarks = "现场施工安全隐患整改及质量抽查记录",
      autoTime = false,
      time = "2026-10-14 19:26:59"
    )

    val lines = config.generateWatermarkLines()

    // Verify address is split into exactly 2 lines:
    // First line: 11 characters
    assertTrue(lines.contains("地址: 浙江省金华市武义县中源"))
    // Second line: 10 characters
    assertTrue(lines.contains("中路52号白洋派出所"))

    // Verify remarks is also split into 11-char lines
    assertTrue(lines.contains("备注: 现场施工安全隐患整改及"))
    assertTrue(lines.contains("质量抽查记录"))

    // Verify time is intact
    assertTrue(lines.contains("时间: 2026-10-14 19:26:59"))
  }

  @Test
  fun testCustomTimeOverrideWhenAutoTimeDisabled() {
    val customTime = "2025-05-20 13:14:00"
    val config = WatermarkConfig(
      time = customTime,
      autoTime = false
    )
    assertEquals(customTime, config.getEffectiveTime())
    val lines = config.generateWatermarkLines()
    assertTrue(lines.contains("时间: $customTime"))
  }
}

