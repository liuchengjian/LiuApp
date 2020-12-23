package com.liucj.liuapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.liucj.liu_ui.tab.bottom.LiuTabBottom
import com.liucj.liu_ui.tab.bottom.LiuTabBottomInfo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tabBottom = findViewById<LiuTabBottom>(R.id.mLiuTabBottom)
        val homeInfo = LiuTabBottomInfo(
            "首页",
            "fonts/iconfont.ttf",
            getString(R.string.if_home),
            null,
            "#ff000000",
            "#ffd44949"
        )
        tabBottom.setLiuTabInfo(homeInfo)
    }
}