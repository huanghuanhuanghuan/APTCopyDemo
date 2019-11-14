package com.example.testcompiler

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.annotation.MyAnnotation

@MyAnnotation
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val vh = TitleViewHolder()
    }
}
