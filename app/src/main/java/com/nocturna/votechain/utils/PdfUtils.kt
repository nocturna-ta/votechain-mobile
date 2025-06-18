package com.nocturna.votechain.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Utility class untuk menangani operasi PDF
 */
object PdfUtils {
    private const val TAG = "PdfUtils"

    /**
     * Download PDF dari ResponseBody dan buka dengan aplikasi eksternal
     * @param context Android context
     * @param responseBody ResponseBody dari API
     * @param fileName Nama file yang akan disimpan
     * @param candidateName Nama kandidat untuk penamaan file
     */
    suspend fun downloadAndOpenPdf(
        context: Context,
        responseBody: ResponseBody,
        candidateName: String = "Kandidat"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Buat nama file yang unik
            val timestamp = System.currentTimeMillis()
            val fileName = "Program_Kerja_${candidateName}_$timestamp.pdf"

            // Simpan file ke internal storage
            val file = saveResponseBodyToFile(context, responseBody, fileName)

            if (file != null) {
                // Pindah ke Main thread untuk operasi UI
                withContext(Dispatchers.Main) {
                    openPdfWithChooser(context, file)
                }
                Log.d(TAG, "PDF berhasil didownload dan dibuka: ${file.absolutePath}")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Gagal menyimpan file PDF")
                Result.failure(Exception("Gagal menyimpan file PDF"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saat download dan buka PDF", e)
            Result.failure(e)
        }
    }

    /**
     * Simpan ResponseBody ke file
     */
    private fun saveResponseBodyToFile(
        context: Context,
        responseBody: ResponseBody,
        fileName: String
    ): File? {
        return try {
            // Buat direktori di internal storage
            val documentsDir = File(context.filesDir, "documents")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val file = File(documentsDir, fileName)

            val inputStream: InputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d(TAG, "File saved successfully: ${file.absolutePath}")
            file
        } catch (e: IOException) {
            Log.e(TAG, "Error saving file", e)
            null
        }
    }

    /**
     * Buka PDF menggunakan aplikasi eksternal dengan chooser
     */
    private fun openPdfWithChooser(context: Context, file: File) {
        try {
            // Gunakan FileProvider untuk mendapatkan URI yang aman
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Buat intent untuk membuka PDF
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Buat chooser untuk memilih aplikasi
            val chooserIntent = Intent.createChooser(
                intent,
                "Pilih aplikasi untuk membuka dokumen"
            )

            // Pastikan ada aplikasi yang bisa menangani intent
            if (chooserIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooserIntent)
            } else {
                // Fallback: coba buka dengan browser atau aplikasi lain
                openWithAlternativeApps(context, uri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening PDF", e)
            Toast.makeText(
                context,
                "Tidak dapat membuka dokumen. Pastikan Anda memiliki aplikasi PDF reader.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Alternatif pembukaan file jika tidak ada PDF reader
     */
    private fun openWithAlternativeApps(context: Context, uri: Uri) {
        try {
            // Coba dengan ACTION_VIEW umum
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            val chooserIntent = Intent.createChooser(
                intent,
                "Buka dokumen dengan"
            )

            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.e(TAG, "No app available to open PDF", e)
            Toast.makeText(
                context,
                "Tidak ada aplikasi yang tersedia untuk membuka dokumen PDF. Silakan install PDF reader terlebih dahulu.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Bersihkan file PDF yang sudah tidak diperlukan
     */
    fun cleanupOldPdfFiles(context: Context, olderThanHours: Long = 24) {
        try {
            val documentsDir = File(context.filesDir, "documents")
            if (documentsDir.exists() && documentsDir.isDirectory) {
                val currentTime = System.currentTimeMillis()
                val cutoffTime = currentTime - (olderThanHours * 60 * 60 * 1000)

                documentsDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            Log.d(TAG, "Cleaned up old file: ${file.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old files", e)
        }
    }
}