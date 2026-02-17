package com.songify.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.ComponentActivity
import com.songify.app.ui.crash.CrashPage

class CrashActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var crashData = intent.getStringExtra("CrashData")
        setContent {
            CrashPage(crashData.orEmpty())
        }
    }
}