package com.example.bustops;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

public class CustomInfoWindowAdapter(private var mContext: Context) : GoogleMap.InfoWindowAdapter {

    private var mWindow: View =
        LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null)

    private fun renderWindowText(p0: Marker?, view: View) {

        val title: String = p0!!.getTitle()
        val tvTitle = view.findViewById(R.id.title) as TextView

        if(!title.equals("")) {
            tvTitle.setText(title);
        }

        val snippet: String = p0.getSnippet()
        val tvSnippet = view.findViewById(R.id.snippet) as TextView

        if(!snippet.equals("")){
            tvSnippet.setText(snippet)
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
