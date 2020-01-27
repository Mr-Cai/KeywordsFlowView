package com.txt.flow;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String[] keywords = {"Apple", "Android", "呵呵",
            "高富帅", "女神", "拥抱", "旅行", "爱情", "屌丝", "搞笑", "暴走漫画", "重邮", "信科",
            "唯美", "汪星人", "秋天", "雨天", "科幻", "黑夜",
            "孤独", "星空", "东京食尸鬼", "金正恩", "张全蛋", "东京热", "陈希妍",
            "明星", "NBA", "马云", "码农", "动漫", "时尚", "熊孩子", "地理", "伤感",
            "二次元"
    };

    KeywordsFlowView keywordsFlowView;
    Button flow_in;
    Button flow_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flow_in = findViewById(R.id.flowInBTN);
        flow_out = findViewById(R.id.flowOutBTN);
        keywordsFlowView = findViewById(R.id.keywordsFlowView);
        // 设置每次随机飞入文字的个数
        keywordsFlowView.setTextShowSize(15);
        // 设置是否允许滑动屏幕切换文字
        keywordsFlowView.shouldScrollFlow(true);
        // 开始展示
        keywordsFlowView.show(keywords, KeywordsFlow.ANIMATION_IN);
        flow_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keywordsFlowView.show(keywords, KeywordsFlow.ANIMATION_IN);   // 文字随机飞入
            }
        });
        flow_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keywordsFlowView.show(keywords, KeywordsFlow.ANIMATION_OUT);   // 文字随机飞出
            }
        });
        // 设置文字的点击点击监听
        keywordsFlowView.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, ((TextView) v).getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
