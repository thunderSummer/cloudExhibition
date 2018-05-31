package tech.soulike.yunzhan.cloudexhibition.base

import org.litepal.crud.DataSupport

/**
 * Created by thunder on 18-3-7.
 *
 */
class ResourceData :DataSupport(){
    lateinit var adName: String
    lateinit var adMd5: String
    lateinit var adUrl: String
    var adId:Int = 0
    var adTime:Int = 0
    var qrcode:Int=0
    lateinit var qrCodePosition:String
    var qrCodeUpdate:Int=-1
}