package kr.co.kimberly.wma.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler

object Utils {

    /**
     * @param context : 해당 액티비티 context
     * @param activity : 이동하려는 액티비티명
     * @param backable : 이동하고 난 후 이전 액티비티를 종료 시켜주고 싶을 때 사용
     */
    fun moveToPage(context: Context, activity: Activity, backable: Boolean){
            // 앱의 MainActivity로 넘어가기
            val intent = Intent(context, activity::class.java)
            context.startActivity(intent)

            // 현재 액티비티 닫기
            if(!backable){
                (context as Activity).finish()
            }
    }

}