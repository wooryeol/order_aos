package kr.co.kimberly.wma.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Window
import android.view.WindowManager

object Utils {

    /**
     * @param context : 해당 액티비티 context
     * @param activity : 이동하려는 액티비티명
     * @param finish : 이동하고 난 후 이전 액티비티를 종료 시켜주고 싶을 때 사용
     */
    fun moveToPage(context: Context, activity: Activity, finish: Boolean? = null){
        // 앱의 MainActivity로 넘어가기
        val intent = Intent(context, activity::class.java)
        context.startActivity(intent)

        // 현재 액티비티 닫기

        if(finish != null){
            if (finish) {
                (context as Activity).finish()
            }
        }
    }

    /**
     * 다이얼로그 사이즈 변경
     */
    fun dialogResize(context: Context, window: Window?) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val width = 1.0f
        val height = 0.7f

        if (Build.VERSION.SDK_INT < 30) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)

            val x = (size.x * width).toInt()
            val y = (size.y * height).toInt()

            window?.setLayout(x, y)
        } else {
            val rect = windowManager.currentWindowMetrics.bounds
            val x = (rect.width() * width).toInt()
            val y = (rect.height() * height).toInt()

            window?.setLayout(x, y)
        }
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

}