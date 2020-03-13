package com.example.bustops

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.fragment_favorites.*


class FavoritesFragment(context: Context) : Fragment() {

    var markers =  mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        checkSelected()
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    private fun checkSelected () {
        // Adding titles of favorite stops to favorite page
        for (i in markers) {
            settings_txt.text = i.title
        }
    }

}
