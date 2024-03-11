package com.improveskills.habitstracker.Ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.improveskills.habitstracker.SharedPrefData
import com.improveskills.habitstracker.databinding.ActivitySplashMainBinding

class Splash_main : AppCompatActivity() {

    lateinit var binding: ActivitySplashMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPrefData= SharedPrefData(this)
        if (sharedPrefData.LoadBoolean("newUser"))
            startActivity(Intent(this@Splash_main, MainActivity::class.java))

        binding.buttonStart.setOnClickListener {
            sharedPrefData.SaveBoolean("newUser",true)
            startActivity(Intent(this@Splash_main, MainActivity::class.java))
        }




    }
}