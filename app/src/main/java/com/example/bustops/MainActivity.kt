package com.example.bustops

import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ctx = applicationContext
        Configuration.getInstance().load(
            ctx,
            PreferenceManager.getDefaultSharedPreferences(ctx)
        )

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                val msg =
                    "latitude: ${task.result!!.latitude} and longitude: ${task.result!!.longitude}"
                main_textview.text = msg
                map.setTileSource(TileSourceFactory.MAPNIK)
                //map.setBuiltInZoomControls(true)
                map.setMultiTouchControls(true)
                map.controller.setZoom(17.0)
                map.controller.setCenter(GeoPoint(task.result!!.latitude, task.result!!.longitude))
            }
        }
        fetchJson()
    }

    fun fetchJson() {
        println("Fetching JSON")

        val url =
            "https://services1.arcgis.com/sswNXkUiRoWtrx0t/arcgis/rest/services/HSL_pysakit_kevat2018/FeatureServer/0/query?where=1%3D1&outFields=REI_VOIM,NIMI1&outSR=4326&f=json"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                println(body)

                val gson = GsonBuilder().create()
                val stops = gson.fromJson(body, Stops::class.java)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }
        })
    }
}

class Stops(val features: List<Stop>)
class Stop(val attributes: Attributes, val geometry: Geometry)
class Attributes(val NIMI1: String)
class Geometry(val x: Float, val y: Float)

