package com.haldny.estudo.workmanager

import android.util.Log
import android.widget.Toast
import androidx.work.Worker

class ValidateWorker : Worker() {

    companion object {
        const val EMPTY_STRING = ""
        const val KEY_UNCOMPRESS_FILE = "uncompress_file"
        const val TAG = "ValidateWorker"
    }

    override fun doWork(): WorkerResult {
        val filePath = inputData.getString(KEY_UNCOMPRESS_FILE, EMPTY_STRING)
        Log.d(TAG, "File path: $filePath")

        val result = validateContent(filePath)

        Log.d(TAG, "Result: $result")

        if (result) {
            Log.d(TAG, "Content is valid")
            return WorkerResult.SUCCESS
        }

        Log.d(TAG, "Content is invalid")
        return WorkerResult.FAILURE
    }

    private fun validateContent(sourcePath: String): Boolean {
        //TODO: validate
        return true
    }

}