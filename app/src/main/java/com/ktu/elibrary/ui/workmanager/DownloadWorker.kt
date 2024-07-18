package com.ktu.elibrary.ui.workmanager

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class DownloadWorker(private val context : Context, workerParams : WorkerParameters)
    : Worker(context,workerParams) {
    override fun doWork(): Result {
        val url = inputData.getString("url")
        val title = inputData.getString("title")
        try {
            val request =
                DownloadManager.Request(Uri.parse(url))
            request.apply {
                setTitle(title)
                setDescription("Downloading")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"$title.pdf")

                val downloadManager : DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
            }

            return Result.success()

        }catch (e : Exception){
            return Result.failure()
        }
    }

    private suspend fun trackDownloadProgress(downloadManager: DownloadManager, downloadId: Long) {
        var isDownloading = true
        while (isDownloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val totalBytes =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if (totalBytes > 0) {
                    val progress = (bytesDownloaded * 100 / totalBytes)
                    setProgressAsync(
                        Data.Builder().putInt("progress", progress).build()
                    )
                }

                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                    isDownloading = false
                }
            }
            cursor.close()
            delay(1000)
        }
    }

}