package tech.soulike.yunzhan.cloudexhibition.base

/**
 * Created by thunder on 18-3-6.
 * This class is designed to parse json data, where the fields and attributes may have multiple meanings
 */
class JsonData {
    lateinit var data: Data
    lateinit var ad: List<Advertise>

    class Advertise {
        lateinit var ad_name: String
        lateinit var ad_md5: String
        lateinit var ad_url: String
        var ad_id:Int = 0
        var ad_time:Int = 0
        lateinit var ad_target:String
        var qrcode:Int=0
        lateinit var ad_qrcode_position:String
        var ad_type:Int=0
        var ad_qrcode_update:Int=-1
    }
    class Data {
        lateinit var url: String
        /**
         * If the md5 is changed , next should request json file in the polling system
         */
        var md5: String? = null
        /**
         * Representative polling interval in the polling system
         */
        lateinit var time: String
        /**
         * In the request 'gain_json', this field represents resource file's url
         */
        lateinit var json_url: String
        /**
         * In checkBind
         * 1 represents the completion of the binding, 
         * 0 represents the unfinished binding,
         * 2 represents the screen does not exist
         */
        var is_user: Int = 0
        /**
         * Representative self-start time in the polling system
         */
        var auto_time: String?= null

        lateinit var qr_url:String

    }
}