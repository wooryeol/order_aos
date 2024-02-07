package kr.co.kimberly.wma.menu.collect

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.databinding.ActCollectBinding

class CollectActivity : AppCompatActivity() {

    private lateinit var mBinding: ActCollectBinding
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActCollectBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }
}
