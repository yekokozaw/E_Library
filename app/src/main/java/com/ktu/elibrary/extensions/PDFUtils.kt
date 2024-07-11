package com.ktu.elibrary.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PDFUtils {
    fun extractFirstPage(context: Context, pdfFile: File): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(0)
                val width = context.resources.displayMetrics.densityDpi / 72 * page.width
                val height = context.resources.displayMetrics.densityDpi / 72 * page.height
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
            }

            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    fun saveInputStreamToFile(context: Context, inputStream: InputStream, fileName: String): File? {
        return try {
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}