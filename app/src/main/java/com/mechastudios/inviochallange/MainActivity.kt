package com.mechastudios.inviochallange


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.SearchView.OnQueryTextListener
import androidx.core.view.isVisible
import com.mechastudios.inviochallange.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private lateinit var movieArrayList : ArrayList<Movies>
    private lateinit var ePosterImg:String
    private lateinit var eMovieName:String
    private lateinit var eMovieYear:String
    private lateinit var eActors:String
    private lateinit var eScoreImdb:String
    private lateinit var eScoreMetacritic:String
    private lateinit var eScoreTomato:String
    private lateinit var ePlot:String
    private lateinit var eGenre:String
    private  var isSearched:Boolean = false
    private val blankData=ArrayList<Movies>()


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.notFoundText.isVisible = false
        initiliazeListView()
        binding.loading.isVisible = false

        binding.movieList.setOnItemClickListener { adapterView, view, i, l ->

            if (isSearched)
            {
                //Eğer arama yapılarak detay sayfasına gidiliyor ise
                goToDetailsActivity()
            }
            else
            {
                //Ana ekrandaki hazır filmlere tıklanarak detay sayfasına gidiliyor ise
                initializeExtras(i)
            }

        }
        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener
        {
            override fun onQueryTextSubmit(p0: String?): Boolean
            {
                binding.searchView.clearFocus()
                return false
            }

            private var textChangeCountDownJob: Job? = null
            override fun onQueryTextChange(p0: String?): Boolean
            {
                textChangeCountDownJob?.cancel()
                if (p0 != null)
                {
                    if (p0.isNotEmpty())
                    {
                        binding.loading.isVisible = true
                        textChangeCountDownJob = CoroutineScope(IO).launch {
                            delay(600)
                            jsonResult(getResultFromApi(p0))
                        }
                    }
                    else
                    {
                        isSearched = false
                        binding.movieList.isClickable = false
                        binding.notFoundText.isVisible = false
                        clearListView(movieArrayList, false)
                        binding.loading.isVisible = false
                    }
                }
                return false
            }
        })
    }

    private suspend fun getResultFromApi(word: String?): String
    {
        //API bağlantısı

        logThread("getResultFromApi")
        val url = "https://www.omdbapi.com/?apikey=ed4cf55&t=$word"
        val res:String
        val connection= URL(url).openConnection() as HttpsURLConnection

        try
        {
            connection.connect()
            res = connection.inputStream.use {it.reader().use {reader ->reader.readText()}}
        }
        finally {
            connection.disconnect()
        }
        return res
    }

    private fun logThread(methodName: String)
    {
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }

    private suspend fun jsonResult(jsonString:String)
    {
        //API'dan dönen bilgiler ayıklanacak

        eScoreImdb ="N/A"
        eScoreTomato ="N/A"
        eScoreMetacritic ="N/A"

        val jsonObject : JSONObject? = JSONObject(jsonString)
        val list=ArrayList<Movies>()
        if (jsonObject != null)
        {
            val isResponse = jsonObject.getString("Response")
            if (isResponse.equals("True"))
            {
                isSearched = true
                list.add(
                    Movies(
                        jsonObject.getString("Title"),
                        jsonObject.getString("Year"),
                        jsonObject.getString("Actors"),
                        jsonObject.getString("Poster")
                    )
                )
                ePosterImg = jsonObject.getString("Poster")
                eMovieName = jsonObject.getString("Title")
                eMovieYear = jsonObject.getString("Year")
                eActors = jsonObject.getString("Actors")
                eGenre = jsonObject.getString("Genre")


                // En başarılı filmlerde genellikle 3 firmanın verisi olduğu için sadece onlara bakılacak
                // imdb, metacritic, rotten tomatoes
                // Örn: Lord of The Rings, Batman
                // Veri varsa veriyi detay ekranına gönderecek, yoksa veri = N/A

                val jsonObjectRatings : JSONArray  = jsonObject.getJSONArray("Ratings")
                (0 until jsonObjectRatings.length()).forEach {
                    val rating = jsonObjectRatings.getJSONObject(it)
                    try
                    {
                        val ratingCompany : String = rating.getString("Source")
                        if (ratingCompany.equals("Internet Movie Database"))
                        {
                            eScoreImdb = rating.getString("Value")
                        }
                        if (ratingCompany.equals("Rotten Tomatoes"))
                        {
                            eScoreTomato = rating.getString("Value")
                        }
                        if(ratingCompany.equals("Metacritic"))
                        {
                            eScoreMetacritic = rating.getString("Value")
                        }

                    }
                    catch (e: Exception)
                    {
                        Log.d("Ratings", "Ratings could not be found. ")
                    }

                }
                ePlot= jsonObject.getString("Plot")
                setOnMainThread(list)
            }
            else
            {
                setOnMainThread(blankData)
            }
        }
    }

    private fun setUI(input : ArrayList<Movies>)
    {
        if (input.isNotEmpty())
        {
            binding.movieList.isClickable = true
            binding.movieList.adapter = ListAdapter(this, input)
            binding.notFoundText.isVisible = false
            val animation = AnimationUtils.loadAnimation(this, R.anim.search_anim)
            binding.movieList.startAnimation(animation)
        }
        else
        {
            clearListView(input,true)
        }
        binding.loading.isVisible = false
    }

    private suspend fun setOnMainThread(input : ArrayList<Movies>)
    {
        withContext(Main)
        {
            setUI(input)
        }
    }

    private fun clearListView(input : ArrayList<Movies>,isStillSearching : Boolean)
    {
        binding.movieList.isClickable = false
        binding.movieList.adapter = ListAdapter(this,input)
        binding.notFoundText.isVisible = isStillSearching
    }

    private fun initiliazeListView ()
    {
        //Arama ekranında hazır bulunan filmlerin bilgileri

        val name = arrayOf(
                "The Lord of the Rings: The Fellowship of the Ring",
                "Batman",
                "Deadpool",
                "The Matrix Resurrections"
        )
        val year = arrayOf(
                "2001",
                "1989",
                "2016",
                "2021"
        )
        val actors = arrayOf(
                "Elijah Wood, Ian McKellen, Orlando Bloom",
                "Michael Keaton, Jack Nicholson, Kim Basinger",
                "Ryan Reynolds, Morena Baccarin, T.J. Miller",
                "Keanu Reeves, Carrie-Anne Moss, Yahya Abdul-Mateen II"
        )
        val poster = arrayOf(
                "https://m.media-amazon.com/images/M/MV5BN2EyZjM3NzUtNWUzMi00MTgxLWI0NTctMzY4M2VlOTdjZWRiXkEyXkFqcGdeQXVyNDUzOTQ5MjY@._V1_SX300.jpg",
                "https://m.media-amazon.com/images/M/MV5BMTYwNjAyODIyMF5BMl5BanBnXkFtZTYwNDMwMDk2._V1_SX300.jpg",
                "https://m.media-amazon.com/images/M/MV5BYzE5MjY1ZDgtMTkyNC00MTMyLThhMjAtZGI5OTE1NzFlZGJjXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_SX300.jpg",
                "https://m.media-amazon.com/images/M/MV5BMGJkNDJlZWUtOGM1Ny00YjNkLThiM2QtY2ZjMzQxMTIxNWNmXkEyXkFqcGdeQXVyMDM2NDM2MQ@@._V1_SX300.jpg"
        )

        movieArrayList = ArrayList()
        for (i in name.indices) {
            val movie = Movies(name[i],year[i],actors[i],poster[i])
            movieArrayList.add(movie)
        }
        binding.movieList.isClickable = true
        binding.movieList.adapter = ListAdapter(this, movieArrayList)
    }
    private fun goToDetailsActivity()
    {
        val extra = Intent(this,DetailsActivity::class.java)
        extra.putExtra("ePosterImg",ePosterImg)
        extra.putExtra("eMovieName",eMovieName)
        extra.putExtra("eMovieYear",eMovieYear)
        extra.putExtra("eActors",eActors)
        extra.putExtra("eScoreImdb",eScoreImdb)
        extra.putExtra("eScoreMetacritic",eScoreMetacritic)
        extra.putExtra("eScoreTomato",eScoreTomato)
        extra.putExtra("ePlot",ePlot)
        extra.putExtra("eGenre",eGenre)
        startActivity(extra)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private fun initializeExtras(i:Int)
    {
        //Arama ekranında hazır bulunan filmlerin detay sayfasına gidecek bilgileri

        val name = arrayOf(
                "The Lord of the Rings: The Fellowship of the Ring",
                "Batman",
                "Deadpool",
                "The Matrix Resurrections"
        )
        val year = arrayOf(
                "2001",
                "1989",
                "2016",
                "2021"
        )
        val actors = arrayOf(
                "Elijah Wood, Ian McKellen, Orlando Bloom",
                "Michael Keaton, Jack Nicholson, Kim Basinger",
                "Ryan Reynolds, Morena Baccarin, T.J. Miller",
                "Keanu Reeves, Carrie-Anne Moss, Yahya Abdul-Mateen II"
        )
        val poster = arrayOf(
                "https://m.media-amazon.com/images/M/MV5BN2EyZjM3NzUtNWUzMi00MTgxLWI0NTctMzY4M2VlOTdjZWRiXkEyXkFqcGdeQXVyNDUzOTQ5MjY@._V1_SX300.jpg",
                "https://m.media-amazon.com/images/M/MV5BMTYwNjAyODIyMF5BMl5BanBnXkFtZTYwNDMwMDk2._V1_SX300.jpg",
                "https://m.media-amazon.com/images/M/MV5BYzE5MjY1ZDgtMTkyNC00MTMyLThhMjAtZGI5OTE1NzFlZGJjXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_SX300.jpg",
                "https://m.media-amazon.com/images/M/MV5BMGJkNDJlZWUtOGM1Ny00YjNkLThiM2QtY2ZjMzQxMTIxNWNmXkEyXkFqcGdeQXVyMDM2NDM2MQ@@._V1_SX300.jpg"
        )
        val scoreImdb = arrayOf(
                "8.8",
                "7.5",
                "8.0",
                "5.7"
        )
        val scoreMetacritic = arrayOf(
                "92/100",
                "69/100",
                "65/100",
                "63/100"
        )
        val scoreTomato = arrayOf(
                "91%",
                "72%",
                "85%",
                "63%"
        )
        val plot = arrayOf(
                "A meek Hobbit from the Shire and eight companions set out on a journey to destroy the powerful One Ring and save Middle-earth from the Dark Lord Sauron.",
                "The Dark Knight of Gotham City begins his war on crime with his first major enemy being Jack Napier, a criminal who becomes the clownishly homicidal Joker.",
                "A wisecracking mercenary gets experimented on and becomes immortal but ugly, and sets out to track down the man who ruined his looks.",
                "Return to a world of two realities: one, everyday life; the other, what lies behind it. To find out if his reality is a construct, to truly know himself," +
                        " Mr. Anderson will have to choose to follow the white rabbit once more"
        )
        val genre = arrayOf(
                "Action, Adventure, Drama",
                "Action, Adventure",
                "Action, Adventure, Comedy",
                "Action, Sci-F"
        )

                val extra = Intent(this,DetailsActivity::class.java)
                extra.putExtra("ePosterImg",poster[i])
                extra.putExtra("eMovieName",name[i])
                extra.putExtra("eMovieYear",year[i])
                extra.putExtra("eActors",actors[i])
                extra.putExtra("eScoreImdb",scoreImdb[i])
                extra.putExtra("eScoreMetacritic",scoreMetacritic[i])
                extra.putExtra("eScoreTomato",scoreTomato[i])
                extra.putExtra("ePlot",plot[i])
                extra.putExtra("eGenre",genre[i])
                startActivity(extra)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }
}
