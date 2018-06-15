package com.haldny.estudo.workmanager

import android.util.Log
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class UncompressWorker : Worker() {

    companion object {
        const val EMPTY_STRING = ""
        const val KEY_DOWNLOAD_FILE = "key_download_file"
        const val KEY_UNCOMPRESS_FILE = "uncompress_file"
        const val TAG = "UncompressWorker"
        const val BUFFER_SIZE = 1024
        const val TEMP_FOLDER_PATH = "temp"
    }

    override fun doWork(): WorkerResult {
        val filePath = inputData.getString(KEY_DOWNLOAD_FILE, EMPTY_STRING)
        Log.d(TAG, "File path: $filePath")

        val result = unzipFile(filePath, getTempFolder()?.absolutePath)

        Log.d(TAG, "Result: $result")

        if (result) {
            outputData = Data.Builder().putString(KEY_UNCOMPRESS_FILE,
                    getTempFolder()?.absolutePath).build()

            Log.d(TAG, "Uncompress completed")

            return WorkerResult.SUCCESS
        }

        Log.d(TAG, "Uncompress failed")
        return WorkerResult.FAILURE
    }

    private fun unzipFile(sourcePath: String, outputPath: String?): Boolean {
        var toReturn : Boolean = false
        var fileInputStream : FileInputStream? = null
        var zipInputStream : ZipInputStream? = null
        var fileOutputStream : FileOutputStream? = null
        var bufferedOutputStream : BufferedOutputStream? = null

        try {
            fileInputStream = FileInputStream(sourcePath)
            zipInputStream = ZipInputStream(fileInputStream)

            val buffer = ByteArray(BUFFER_SIZE)

            var zipEntry: ZipEntry? = zipInputStream?.nextEntry

            while(zipEntry != null) {

                Log.d(TAG, "Zip entry: ${zipEntry?.name}")

                if (zipEntry.isDirectory) {
                    createDir(zipEntry.name)
                } else {
                    val fileToExtract = zipEntry.name;
                    val createdFile = createFile(outputPath + File.separator + fileToExtract)

                    fileOutputStream = FileOutputStream(createdFile)
                    bufferedOutputStream = BufferedOutputStream(fileOutputStream)

                    var readBytes: Int = zipInputStream.read(buffer)
                    while (readBytes > 0) {
                        bufferedOutputStream.write(buffer, 0, readBytes)
                        readBytes = zipInputStream.read(buffer)
                    }

                    zipInputStream?.closeEntry()
                    bufferedOutputStream?.close()
                    fileOutputStream?.close()
                }

                zipEntry = zipInputStream?.nextEntry
            }

            toReturn = true
            Log.d(TAG, "Unzip complete. File name: $sourcePath")
        } catch (fne: FileNotFoundException) {
            Log.e(TAG, "File not found. File name: $sourcePath")
        } catch (ioe: IOException) {
            Log.e(TAG, "Error reading file. File name: $sourcePath")
        } catch (ex: Exception) {
            Log.e(TAG, "Error. Filename: $sourcePath")
        } finally {
            fileOutputStream?.close()
            bufferedOutputStream?.close()
            fileInputStream?.close()
            zipInputStream?.close()
        }

        return toReturn
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


    private fun createDir(dir: String): Boolean {
        val f = File(dir)
        return createDir(f)
    }

    private fun createDir(f: File): Boolean {
        var result = true

        if (!f.isDirectory || !f.exists()) {
            result = f.mkdirs()
        }

        return result
    }

    @Throws(IOException::class)
    private fun createFile(path: String): File {
        val newFile = File(path)

        val bufferDirectory = newFile.parent
        val directory = File(bufferDirectory)

        val dirExists = createDir(directory)

        val isCreated = dirExists && newFile.exists() || newFile.createNewFile()
        if (!isCreated) {
            throw IOException("No file created!")
        }

        return newFile
    }

}