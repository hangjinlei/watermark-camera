package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.media.ExifInterface
import com.example.data.WatermarkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

/**
 * Handles rendering the professional watermark onto Bitmaps and saving them to device storage.
 */
object WatermarkRenderer {

    /**
     * Draws watermark directly onto the bitmap and returns the result.
     */
    fun drawWatermark(
        srcBitmap: Bitmap,
        config: WatermarkConfig,
        burnHintBadge: Boolean = false
    ): Bitmap {
        val width = srcBitmap.width
        val height = srcBitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw original photo
        canvas.drawBitmap(srcBitmap, 0f, 0f, null)

        val baseDimension = min(width, height).toFloat()
        val scale = config.fontSizeScale

        // Text Paint
        val textSize = baseDimension * 0.025f * scale
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            // Black shadow for contrast against any background
            setShadowLayer(textSize * 0.2f, textSize * 0.08f, textSize * 0.08f, Color.argb(220, 0, 0, 0))
        }

        val paddingLeft = baseDimension * 0.045f
        val paddingBottom = baseDimension * 0.05f
        val lineSpacing = textSize * 0.45f
        val lineHeight = textSize + lineSpacing

        // Prepare watermark lines from bottom up
        val lines = mutableListOf<String>()

        if (config.showRemarks && config.remarks.isNotBlank()) {
            lines.add("备注: ${config.remarks}")
        }
        if (config.showWeather && config.weather.isNotBlank()) {
            lines.add("天气: ${config.weather}")
        }
        if (config.showAltitude && config.altitude.isNotBlank()) {
            lines.add("海拔: ${config.altitude}")
        }
        lines.add("时间: ${config.getEffectiveTime()}")

        // Multi-line wrap for address
        val maxAddressWidth = width - paddingLeft * 2 - (baseDimension * 0.05f)
        val addressFull = "地址: ${config.address}"
        val wrappedAddressLines = wrapText(addressFull, textPaint, maxAddressWidth)
        for (i in wrappedAddressLines.indices.reversed()) {
            lines.add(wrappedAddressLines[i])
        }

        if (config.showCoordinateSystem && config.coordinateSystem.isNotBlank()) {
            lines.add("坐标系: ${config.coordinateSystem}")
        }
        lines.add("纬度: ${config.latitude}")
        lines.add("经度: ${config.longitude}")

        // Draw lines from bottom to top
        var currentY = height - paddingBottom
        for (line in lines) {
            canvas.drawText(line, paddingLeft, currentY, textPaint)
            currentY -= lineHeight
        }

        // Draw pill hint badge if requested (e.g. "单击编辑水印，长按编辑备注")
        if (burnHintBadge && config.showHintBadge) {
            val badgeText = "单击编辑水印，长按编辑备注"
            val badgeTextSize = textSize * 0.85f
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                this.textSize = badgeTextSize
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val bounds = Rect()
            badgePaint.getTextBounds(badgeText, 0, badgeText.length, bounds)

            val badgePadH = badgeTextSize * 0.8f
            val badgePadV = badgeTextSize * 0.4f
            val badgeW = bounds.width() + badgePadH * 2
            val badgeH = bounds.height() + badgePadV * 2

            val badgeRect = RectF(
                paddingLeft,
                currentY - badgeH + (badgePadV * 0.5f),
                paddingLeft + badgeW,
                currentY + (badgePadV * 0.5f)
            )

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(120, 0, 0, 0)
                style = Paint.Style.FILL
            }
            val cornerRadius = badgeH / 2f
            canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, bgPaint)
            canvas.drawText(
                badgeText,
                paddingLeft + badgePadH,
                currentY - (badgePadV * 0.5f),
                badgePaint
            )
        }

        // Draw Center Watermark if enabled (e.g. "现场拍照")
        if (config.showCenterWatermark && config.centerWatermarkText.isNotBlank()) {
            val centerText = config.centerWatermarkText
            val centerTextSize = baseDimension * 0.08f
            val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(45, 255, 255, 255)
                this.textSize = centerTextSize
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = baseDimension * 0.003f
            }
            val cBounds = Rect()
            centerPaint.getTextBounds(centerText, 0, centerText.length, cBounds)
            val cx = (width - cBounds.width()) / 2f
            val cy = (height + cBounds.height()) / 2f - (baseDimension * 0.05f)
            canvas.drawText(centerText, cx, cy, centerPaint)
        }

        // Draw Bottom Right Brand Watermark if enabled (e.g. "经纬水印相机")
        if (config.showBrandWatermark && config.brandWatermarkText.isNotBlank()) {
            val brandText = config.brandWatermarkText
            val brandTextSize = baseDimension * 0.022f * scale
            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(120, 255, 255, 255)
                this.textSize = brandTextSize
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                setShadowLayer(brandTextSize * 0.2f, 1f, 1f, Color.argb(150, 0, 0, 0))
            }
            val bBounds = Rect()
            brandPaint.getTextBounds(brandText, 0, brandText.length, bBounds)
            val bx = width - paddingLeft - bBounds.width()
            val by = height - paddingBottom
            canvas.drawText(brandText, bx, by, brandPaint)
        }

        return output
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val count = paint.breakText(text, start, text.length, true, maxWidth, null)
            if (count > 0) {
                result.add(text.substring(start, start + count))
                start += count
            } else {
                result.add(text.substring(start))
                break
            }
        }
        return result
    }

    /**
     * Fixes bitmap rotation based on EXIF tag.
     */
    fun rotateBitmapIfRequired(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    /**
     * Loads a Bitmap from Uri with orientation correction.
     */
    suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val orientation = inputStream?.use { stream: InputStream ->
                try {
                    val exif = ExifInterface(stream)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } catch (e: Exception) {
                    ExifInterface.ORIENTATION_NORMAL
                }
            } ?: ExifInterface.ORIENTATION_NORMAL

            inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = inputStream?.use { stream: InputStream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext null

            rotateBitmapIfRequired(originalBitmap, orientation)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves watermarked bitmap to public Pictures/WatermarkCamera directory and internal app storage.
     */
    suspend fun saveWatermarkedBitmap(
        context: Context,
        bitmap: Bitmap
    ): File? = withContext(Dispatchers.IO) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "WM_$timeStamp.jpg"

            // Save to app internal pictures directory
            val internalDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "WatermarkCamera")
            if (!internalDir.exists()) internalDir.mkdirs()
            val file = File(internalDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            // Also insert into MediaStore so it immediately appears in system Gallery
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/WatermarkCamera")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
