package com.liucj.liuapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.liucj.lib_picture_selector.PictureView
import com.liucj.liu_ui.tab.bottom.LiuTabBottom
import com.liucj.liu_ui.tab.bottom.LiuTabBottomInfo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mPictureView = findViewById<PictureView>(R.id.mPictureView)
        mPictureView.setAspectRatio(1,1)
        mPictureView.init()
    }
}