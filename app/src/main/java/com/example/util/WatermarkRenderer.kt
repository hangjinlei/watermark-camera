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

        // Text Paint with System Default Font
        val textSize = baseDimension * 0.025f * scale
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            // Black shadow for contrast against any background
            setShadowLayer(textSize * 0.2f, textSize * 0.08f, textSize * 0.08f, Color.argb(220, 0, 0, 0))
        }

        val paddingLeft = baseDimension * 0.045f
        val paddingBottom = baseDimension * 0.05f
        val lineSpacing = textSize * 0.45f
        val lineHeight = textSize + lineSpacing

        // Watermark lines wrapped at 11 characters
        val lines = config.generateWatermarkLines(config.getEffectiveTime())
        val reversedLines = lines.reversed()

        // Draw lines from bottom to top
        var currentY = height - paddingBottom
        for (line in reversedLines) {
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
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
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

        // Draw Center Watermark if enabled (e.g. "现场拍照" with realistic security tilt)
        if (config.showCenterWatermark && config.centerWatermarkText.isNotBlank()) {
            val centerText = config.centerWatermarkText
            val centerTextSize = baseDimension * 0.075f
            val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(50, 255, 255, 255)
                this.textSize = centerTextSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.18f
                setShadowLayer(centerTextSize * 0.12f, 2f, 2f, Color.argb(60, 0, 0, 0))
            }
            val cBounds = Rect()
            centerPaint.getTextBounds(centerText, 0, centerText.length, cBounds)
            canvas.save()
            canvas.rotate(-15f, width / 2f, height / 2f)
            val cx = (width - cBounds.width()) / 2f
            val cy = (height + cBounds.height()) / 2f
            canvas.drawText(centerText, cx, cy, centerPaint)
            canvas.restore()
        }

        // Draw Bottom Right Brand Watermark if enabled (e.g. "经纬水印相机")
        if (config.showBrandWatermark && config.brandWatermarkText.isNotBlank()) {
            val brandText = config.brandWatermarkText
            val brandTextSize = baseDimension * 0.019f
            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(170, 255, 255, 255)
                this.textSize = brandTextSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                setShadowLayer(brandTextSize * 0.15f, 1.5f, 1.5f, Color.argb(150, 0, 0, 0))
            }
            val bBounds = Rect()
            brandPaint.getTextBounds(brandText, 0, brandText.length, bBounds)
            val bx = width - paddingLeft - bBounds.width()
            val by = height - paddingBottom
            canvas.drawText(brandText, bx, by, brandPaint)
        }

        return output
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
