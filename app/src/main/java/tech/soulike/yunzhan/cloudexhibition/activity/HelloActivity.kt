package tech.soulike.yunzhan.cloudexhibition.activity


import android.os.Bundle
import android.os.Handler
import tech.soulike.yunzhan.cloudexhibition.R
import tech.soulike.yunzhan.cloudexhibition.util.startActivity

class HelloActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)
        Handler().postDelayed({ startActivity<MainActivity>()
            finish() }, 1500L)
    }
}
