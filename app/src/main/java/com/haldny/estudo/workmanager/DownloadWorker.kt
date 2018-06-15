package com.haldny.estudo.workmanager

import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DownloadWorker : Worker() {

    companion object {
        const val EMPTY_STRING = ""
        const val KEY_URL = "key_url"
        const val KEY_DOWNLOAD_FILE = "key_download_file"
        const val DOWNLOAD_FILE_NAME = "download.zip"
        const val TAG = "DownloadWorker"
        const val TEMP_FOLDER_PATH = "temp"
    }

    override fun doWork(): WorkerResult {
        Log.d(TAG, "doWork")

        val url = inputData.getString(KEY_URL, EMPTY_STRING)

        Log.d(TAG, "Url: $url")

        if (URLUtil.isValidUrl(url)) {
            Log.d(TAG, "Download File")
            val downloadFile = startDownloadFile(url)
            outputData =  Data.Builder().putString(KEY_DOWNLOAD_FILE, downloadFile?.absolutePath).build()

            Log.d(TAG, "Download completed")
            return WorkerResult.SUCCESS
        }

        Log.d(TAG, "Download failed")
        return WorkerResult.FAILURE
    }

    private fun startDownloadFile(url : String?) : File? {
        var file : File? = null

        try {
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            file = createFile(response, getTempFolder()?.absolutePath
                    + File.separator + DOWNLOAD_FILE_NAME)
            Log.d(TAG, "File downloaded $file")
        } catch (e: IOException) {
            Log.e(TAG, "Download failed: " + url
                    + ", error message: " + e.message)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Download failed: " + url
                    + ", error message: " + e.message)
        }

        return file
    }

    @Throws(IOException::class)
    private fun createFile(response : Response, filename : String) : File? {
        var file: File? = null

        if (response.isSuccessful) {
            file = File(filename)
            try {
                var fileOutputStream = FileOutputStream(file)

                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    Log.d(TAG, "File: $filename  already exists.");
                }

                if (file.canWrite()) {
                    fileOutputStream.write(response.body()?.bytes());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } else {
                    Log.d(TAG, "File: " + filename + " cannot be written");
                    return null;
                }
            } catch (e: IOException) {
                Log.d(TAG, "Error creating " + filename + ". Error message: " + e.message)
                file = null
            }

        } else {
            Log.d(TAG, "Response isn't successful, response code: " + response.code()
                    + " , response message: " + response.message())
        }

        return file
    }

    private fun getTempFolder(): File? {
        var tempFolderName = TEMP_FOLDER_PATH

        val tempDir = File(applicationContext.getFilesDir(), tempFolderName)

        if (!tempDir.exists()) {
            val created = tempDir.mkdirs()
            if (created) {
                return tempDir
            }
        }

        return if (tempDir.isDirectory()) tempDir else null
    }
}