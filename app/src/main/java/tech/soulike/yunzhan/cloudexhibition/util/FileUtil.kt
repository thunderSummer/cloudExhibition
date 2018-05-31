package tech.soulike.yunzhan.cloudexhibition.util

import android.content.Context
import android.os.Environment
import tech.soulike.yunzhan.cloudexhibition.base.MyApplication
import java.io.File
import java.io.IOException

/**
 * Created by thunder on 18-3-6.
 * This class is used to handle file operations designed in the app
 */
object FileUtil {
    fun getResourceHome(): String {
        return getRootPath(MyApplication.context) + "/resource/"
    }

    private fun getResourcePath(resourceId: String): String {
        return getResourceHome() + resourceId
    }

    fun getResourceFile(resourceId: String): File? {
        val file = File(getResourcePath(resourceId))
        if (file.exists()) {
            return null
        } else {
            createFile(file)
        }
        return file

    }

    fun getRootPath(context: Context): String {
        var rootPath = ""
        return if (isSdCardAvailable())
            context.externalCacheDir!!.path
        else
            context.cacheDir.path
    }

    private fun isSdCardAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun createFile(file: File): Boolean {
        try {
            return if (file.parentFile.exists()) {
                file.createNewFile()
            } else {
                createDir(file.parentFile.absolutePath)
                file.createNewFile()
            }
        } catch (i: IOException) {
            i.printStackTrace()
        }

        return false

    }

    fun makeFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) true
        else {
            if (file.isDirectory) createDir(path)
            else createFile(file)
            true
        }
    }

    private fun createDir(path: String): Boolean {
        val file = File(path)
        return if (file.parentFile.exists()) {
            file.mkdir()
        } else {
            createDir(file.parentFile.absolutePath)
            file.mkdir()
        }
    }

    /**
     * 删除指定文件
     *
     * @param file
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deleteFile(file: File): Boolean {
        return deleteFileOrDirectory(file)
    }

    /**
     * 删除指定文件，如果是文件夹，则递归删除
     *
     * @param file
     * @return
     * @throws IOException
     */
    fun deleteFileOrDirectory(file: File?): Boolean {
        try {
            if (file != null && file.isFile) {
                return file.delete()
            }
            if (file != null && file.isDirectory) {
                val childFiles = file.listFiles()
                // 删除空文件夹
                if (childFiles == null || childFiles.size == 0) {
                    return file.delete()
                }
                // 递归删除文件夹下的子文件
                for (i in childFiles.indices) {
                    deleteFileOrDirectory(childFiles[i])
                }
                return file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}