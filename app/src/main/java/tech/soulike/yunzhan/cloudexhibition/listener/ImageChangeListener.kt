package tech.soulike.yunzhan.cloudexhibition.listener

/**
 * Created by thunder on 18-3-7.
 */
interface ImageChangeListener {
    fun onBitmapChange(currentId: Int)
    fun onBitmapPause()
}