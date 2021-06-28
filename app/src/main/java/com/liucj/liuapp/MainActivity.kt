package com.liucj.liuapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.liucj.liu_library.picture.PictureSelectView
import com.liucj.liu_library.picture.PictureStyle
import com.liucj.liu_ui.refresh.LiuOverView
import com.liucj.liu_ui.refresh.LiuRefresh
import com.liucj.liu_ui.refresh.LiuRefreshLayout
import com.liucj.liu_ui.refresh.LiuTextOverView
import com.luck.picture.lib.style.PictureParameterStyle
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mRefreshLayout = findViewById<LiuRefreshLayout>(R.id.mRefreshLayout)
        val overView = LiuTextOverView(this)
        mRefreshLayout.setRefreshOverView(overView)
        mRefreshLayout.setRefreshListener(object : LiuRefresh.LiuRefreshListener {
            override fun enableRefresh(): Boolean {
                return true
            }

            override fun onRefresh() {
                Handler().postDelayed({ mRefreshLayout.refreshFinished() }, 1000)
            }

        })
//        val mPictureView = findViewById<PictureSelectView>(R.id.mPictureView)
//        mPictureView.setNoOnlyCamera(false)
//        mPictureView.setMaxSelectNum(16)
//        mPictureView.setWeChatStyle(false)
//        mPictureView.setShowTopDel(true)
//        mPictureView.setShowTouchDel(true)
//        mPictureView.setCompress(true)
//        mPictureView.setPictureParameterStyle("custom","#2f54ed")
//        mPictureView.init()
//        mPictureView.setPictureSelectListener { selectList ->
//            for (media in selectList) {
//                Log.i("MainActivity", "选择的图片___是否压缩:" + media.isCompressed)
//                Log.i("MainActivity", "选择的图片___压缩:" + media.compressPath)
//                Log.i("MainActivity", "选择的图片___原图:" + media.path)
//                Log.i("MainActivity", "选择的图片___绝对路径:" + media.realPath)
//                Log.i("MainActivity", "选择的图片___是否裁剪:" + media.isCut)
//                Log.i("MainActivity", "选择的图片___裁剪:" + media.cutPath)
//                Log.i("MainActivity", "选择的图片___是否开启原图:" + media.isOriginal)
//                Log.i("MainActivity", "选择的图片___原图路径:" + media.originalPath)
//                Log.i(
//                    "MainActivity",
//                    "选择的图片___Android Q 特有Path:" + media.androidQToPath
//                )
//            }
//        }

    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        mPictureView.onActivityResult(requestCode, resultCode, data)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mPictureView.onDestroy()
//        mPictureView.clearCache()
//    }
}