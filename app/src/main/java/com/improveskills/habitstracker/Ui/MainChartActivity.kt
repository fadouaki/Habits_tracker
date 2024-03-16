package com.improveskills.habitstracker.Ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.improveskills.habitstracker.R
import com.improveskills.habitstracker.databinding.ActivityMainChartBinding

class MainChartActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainChartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            barChart.animation.duration = animationDuration
            barChart.animate(barSet)
            barChart.onDataPointClickListener={ index,_,_ ->
                tvChartName.text =
                    barSet.toList()[index]
                        .second
                        .toString()
            }
        }


    }
    companion object {

        private val barSet = listOf(
            "JAN" to 85F,
            "FEB" to 47F,
            "MAR" to 100F,
            "MAY" to 20.3F,
            "APR" to 50F,
            "JUN" to 40F
        )


        private const val animationDuration = 1000L
    }
}