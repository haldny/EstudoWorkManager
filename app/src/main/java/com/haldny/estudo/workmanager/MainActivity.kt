package com.haldny.estudo.workmanager

import android.app.job.JobScheduler
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.work.*
import androidx.work.WorkManager




class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val btnDownload = findViewById<Button>(R.id.btn_download)
        btnDownload.setOnClickListener({ v -> btnClick()})
    }

    private fun btnClick() {
        Log.d(TAG, "Botao clicado")

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancelAll()

        val inputData = Data.Builder()
                .putString("key_url",
                        "https://drive.google.com/uc?export=download&id=1etMnK20gfz8X7_hcnz2-Rxv-62ImtHvy")
                .build()

        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val downloadWork = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setInputData(inputData)
                .setConstraints(constraints).build()

        val unzipWork = OneTimeWorkRequest.Builder(UncompressWorker::class.java)
                .build()

        val validateWorker = OneTimeWorkRequest.Builder(ValidateWorker::class.java)
                .build()

        val workManager = WorkManager.getInstance()
        workManager.beginWith(downloadWork).then(unzipWork).then(validateWorker).enqueue()

        workManager.getStatusById(downloadWork.id).observe(this,
                android.arch.lifecycle.Observer {
            if (it?.state?.isFinished!!) {
                Toast.makeText(this, "Download finished.", Toast.LENGTH_LONG).show()
            }
        })

        workManager.getStatusById(unzipWork.id).observe(this,
                android.arch.lifecycle.Observer {
            if (it?.state?.isFinished!!) {
                Toast.makeText(this, "Uncompress finished.", Toast.LENGTH_LONG).show()
            }
        })

        workManager.getStatusById(validateWorker.id).observe(this,
                android.arch.lifecycle.Observer {
            if (it?.state?.isFinished!!) {
                Toast.makeText(this, "Content is valid.", Toast.LENGTH_LONG).show()
            }
        })


    }

}