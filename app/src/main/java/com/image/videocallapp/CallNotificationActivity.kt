package com.image.videocallapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.image.videocallapp.databinding.ActivityCallNotificationBinding

class CallNotificationActivity : AppCompatActivity() {

    private var binding:ActivityCallNotificationBinding ?= null
    private var customNotificationManager: CustomNotificationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallNotificationBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }
}