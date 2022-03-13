package com.mechastudios.inviochallange

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.mechastudios.inviochallange.databinding.ActivityDetailsBinding
import com.mechastudios.inviochallange.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso

class DetailsActivity : AppCompatActivity() {
    lateinit var binding : ActivityDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.movieName.text = intent.getStringExtra("eMovieName")
        binding.movieYear.text = intent.getStringExtra("eMovieYear")
        binding.actors.text = intent.getStringExtra("eActors")
        binding.scoreImdb.text = intent.getStringExtra("eScoreImdb")
        binding.scoreMetacritic.text = intent.getStringExtra("eScoreMetacritic")
        binding.scoreTomato.text = intent.getStringExtra("eScoreTomato")
        binding.plot.text = intent.getStringExtra("ePlot")
        binding.genre.text = intent.getStringExtra("eGenre")
        val img_url = intent.getStringExtra("ePosterImg")
        Picasso.get().load(img_url).into(binding.posterImg)

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }
}