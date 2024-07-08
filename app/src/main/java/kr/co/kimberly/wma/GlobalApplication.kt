package kr.co.kimberly.wma

import android.content.Context
import androidx.multidex.MultiDexApplication

class GlobalApplication : MultiDexApplication() {

    companion object {
        var instance: GlobalApplication? = null

        @JvmStatic
        fun newInstance(): GlobalApplication? {
            return if (instance != null) {
                return instance
            } else {
                instance = GlobalApplication()
                return instance
            }
        }

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}