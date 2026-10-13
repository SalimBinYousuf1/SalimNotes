package com.example.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfRendererHelper {

    suspend fun renderFirstPageThumbnail(context: Context, pdfFile: File, width: Int = 300): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                if (!pdfFile.exists() || pdfFile.length() == 0L) return@withContext null
                val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                if (renderer.pageCount == 0) {
                    renderer.close()
                    fileDescriptor.close()
                    return@withContext null
                }
                val page = renderer.openPage(0)
                val height = (width * (page.height.toFloat() / page.width.toFloat())).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                fileDescriptor.close()
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun getPageCount(pdfFile: File): Int {
        return withContext(Dispatchers.IO) {
            try {
                if (!pdfFile.exists()) return@withContext 0
                val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                val count = renderer.pageCount
                renderer.close()
                fileDescriptor.close()
                count
            } catch (e: Exception) {
                0
            }
        }
    }

    suspend fun renderPage(pdfFile: File, pageIndex: Int, width: Int = 1080): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                    renderer.close()
                    fileDescriptor.close()
                    return@withContext null
                }
                val page = renderer.openPage(pageIndex)
                val height = (width * (page.height.toFloat() / page.width.toFloat())).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                fileDescriptor.close()
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun copyUriToFile(context: Context, sourceUri: Uri, destinationFile: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
