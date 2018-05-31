package tech.soulike.yunzhan.cloudexhibition.listener

/**
 * Created by thunder on 18-3-6.
 *
 */
interface DownloadListener {
    fun onProgress(vararg progress: Int)
    fun onSuccess(type: Int)
    fun onFailed()
    fun after()
}