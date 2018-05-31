package tech.soulike.yunzhan.cloudexhibition.base

import org.litepal.crud.DataSupport
import tech.soulike.yunzhan.cloudexhibition.util.FileUtil
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by thunder on 18-3-7.
 *
 */
object ResourceController {
        fun setPicture(originMap: Map<String, String>): ArrayList<String> {
            val file = File(FileUtil.getResourceHome() + "resource/")
            val downLoadString = arrayListOf<String>()
            val downloadId = arrayListOf<String>()
            if (!file.exists() || file.listFiles() == null || file.listFiles().isEmpty()) {
                //代表没有资源
                for ((fileURL) in originMap) {
                    downLoadString.add(fileURL)
                }
                return downLoadString
            }

            val files = file.listFiles()
            val existList = arrayListOf<File>()

            existList.addAll(files)
            try {
                for ((fileURL, fileMD5) in originMap) {
                    var exist = false
                    for (file1 in files) {
                        if (MD5.getFileMD5String(file1).equals(fileMD5,true)) {
                            exist = true
                            existList.remove(file1)
                        }
                    }
                    if (!exist) {
                        downLoadString.add(fileURL)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            for (file1 in existList) {
                FileUtil.deleteFileOrDirectory(file1)
            }
            return downLoadString
        }
}