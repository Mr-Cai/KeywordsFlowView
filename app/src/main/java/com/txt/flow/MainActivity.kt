package com.txt.flow

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keywordsFlowView.setTextShowSize(25)   // 设置每次随机飞入文字的个数
        keywordsFlowView.shouldScrollFlow(true)  // 设置是否允许滑动屏幕切换文字
        keywordsFlowView.show(keywords, KeywordsFlow.ANIMATION_IN)  // 开始展示

        flowInBTN.setOnClickListener {
            keywordsFlowView.show(
                keywords,
                KeywordsFlow.ANIMATION_IN
            ) // 文字随机飞入
        }

        flowOutBTN.setOnClickListener {
            keywordsFlowView.show(
                keywords,
                KeywordsFlow.ANIMATION_OUT
            ) // 文字随机飞出
        }

        // 设置文字的点击点击监听
        keywordsFlowView.setOnItemClickListener(View.OnClickListener { v ->
            Toast.makeText(
                this@MainActivity,
                (v as TextView).text.toString(),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    companion object {
        val keywords = arrayOf(
            "Apple", "Android", "呵呵",
            "高富帅", "女神", "拥抱", "旅行", "爱情", "屌丝", "搞笑", "暴走漫画", "重邮", "信科",
            "唯美", "汪星人", "秋天", "雨天", "科幻", "黑夜",
            "孤独", "星空", "东京食尸鬼", "金正恩", "张全蛋", "东京热", "陈希妍",
            "明星", "NBA", "马云", "码农", "动漫", "时尚", "熊孩子", "地理", "伤感",
            "二次元"
        )
    }
}