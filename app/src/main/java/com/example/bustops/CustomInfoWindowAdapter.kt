package com.example.bustops

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(mContext: Context) : GoogleMap.InfoWindowAdapter {


    @SuppressLint("InflateParams")
    private var mWindow: View =
        LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null)

    private fun renderWindowText(p0: Marker?, view: View) {

        val title: String = p0!!.title
        val tvTitle = view.findViewById(R.id.title) as TextView

        if(title != "") {
            tvTitle.text = title
        }

        val snippet: String = p0.snippet
        val tvSnippet = view.findViewById(R.id.snippet) as TextView

        if(snippet != ""){
            tvSnippet.text = snippet
        }
    }

    override fun getInfoContents(p0: Marker?): View {
        renderWindowText(p0,mWindow)
        return mWindow
    }

    override fun getInfoWindow(p0: Marker?): View {
        renderWindowText(p0,mWindow)
        return mWindow
    }

}
