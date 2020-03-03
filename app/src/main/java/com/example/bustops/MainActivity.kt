package com.example.bustops

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var busStop: Stops

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchJson()
        setContentView(R.layout.activity_main)

        //callWebService()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera

    }

    private fun fetchJson() {

        val url =
            "https://services1.arcgis.com/sswNXkUiRoWtrx0t/arcgis/rest/services/HSL_pysakit_kevat2018/FeatureServer"

        val request = Request
            .Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                println(body)
                val gson = GsonBuilder().create()
                val stop = gson.fromJson(body, Stops::class.java)

                runOnUiThread(){
                    addMarkers(stop)
                }

            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }
        })
    }

    fun addMarkers(stop: Stops) {
        for (i in stop.features){
            if (i.attributes.REI_VOIM == 1 && i.attributes.AIK_VOIM == 1) {
                val loc = LatLng(i.geometry.y, i.geometry.x)
                mMap.addMarker(MarkerOptions().position(loc).title(i.attributes.NIMI1).snippet(i.attributes.REI_VOIM.toString()))
            }

        }
    }



    fun callWebService() {

        val call: retrofit2.Response<HslApi.Model.StopInfo> = HslApi.service.id("HSL:1040129")

        val value = object : retrofit2.Callback<HslApi.Model.StopInfo> {
            override fun onResponse(
                call: retrofit2.Call<HslApi.Model.StopInfo>,
                response: retrofit2.Response<HslApi.Model.StopInfo>?
            ) {

                if (response != null) {
                    val res: HslApi.Model.StopInfo = response.body()!!
                    println(res.name)
                }

            }

            override fun onFailure(call: retrofit2.Call<HslApi.Model.StopInfo>, t: Throwable) {
                println("failed")
            }
        }
        //call.enqueue(value) // asynchronous request }
    }
}

class Stops(val features: List<Stop>)
class Stop(val attributes: Attributes, val geometry: Geometry)
class Attributes(val NIMI1: String, val REI_VOIM: Int, val AIK_VOIM: Int)
class Geometry(val x: Double, val y: Double)