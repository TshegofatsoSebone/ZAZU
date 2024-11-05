package com.example.za


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.za.databinding.ActivityHomeBinding


class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use binding to reference views instead of findViewById
        binding.se.setOnClickListener {
         val setting = Intent(this, Settings::class.java)
            startActivity(setting)
        }

        binding.pr.setOnClickListener {
            val profile = Intent(this, ViewProfile::class.java)
            startActivity(profile)
        }

        binding.ad.setOnClickListener {
            val newObservation = Intent(this, NewObservation::class.java)
            startActivity(newObservation)
        }
        binding.vi.setOnClickListener {
            val exploreD = Intent(this, ObservationList::class.java)
            startActivity(exploreD)
        }
        binding.hotspot.setOnClickListener {
            val findhotspot = Intent(this, FindBirdHotstops::class.java)
            startActivity(findhotspot)
        }
    }
}
