package com.example.customcamera.activity

import android.os.Bundle
import com.example.customcamera.databinding.ActivityHeadBinding

class HeadActivity : BaseActivity() {

    private lateinit var binding: ActivityHeadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreen(this)
        binding = ActivityHeadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.btnGo.setOnClickListener {
            callMainActivity(this)
        }
    }
}

