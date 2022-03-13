package com.mechastudios.inviochallange

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class ListAdapter(private val context : Activity, private val arrayList: ArrayList<Movies>) : ArrayAdapter<Movies>(context,
    R.layout.movie_listview_items,arrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = inflater.inflate(R.layout.movie_listview_items,null)
        val imageView : ImageView = view.findViewById(R.id.posterImg)
        val movieName : TextView = view.findViewById(R.id.movieName)
        val movieYear : TextView = view.findViewById(R.id.movieYear)
        val shortPlot : TextView = view.findViewById(R.id.actors)
        val image_url : String = arrayList[position].posterUrl

        //println("debug: ${image_url}")

        if (arrayList[position].name.isNotEmpty()) {
            Picasso.get().load(image_url).into(imageView)
            movieName.text = arrayList[position].name
            movieYear.text = arrayList[position].year
            shortPlot.text = arrayList[position].actors
        }



        return view
    }

}