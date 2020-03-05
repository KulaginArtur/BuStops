package com.example.bustops

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.bustops.model.StopPost
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var homeFragment: HomeFragment
    lateinit var arFragment: ArFragment
    lateinit var settingsFragment: SettingsFragment

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        homeFragment = HomeFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, homeFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        btm_nav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.home -> {
                    homeFragment = HomeFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, homeFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.ar -> {
                    arFragment = ArFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, arFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.settings -> {
                    settingsFragment = SettingsFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, settingsFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }

            }

            true
        }

        //fetchJson()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun fetchJson() {
        val url =
            "https://services1.arcgis.com/sswNXkUiRoWtrx0t/arcgis/rest/services/HSL_pysakit_kevat2018/FeatureServer/0/query?where=1%3D1&outFields=*&outSR=4326&f=json"

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
                callWebService(stop)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }
        })
    }

    fun callWebService(stops: Stops) {

        // RETROFIT BUILDER

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.digitransit.fi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(HslApi::class.java)

        // for (i in stops.features) {}
        val stopPost = StopPost().query.toGraphQueryString()

        val call = service.sentStopData(stopPost)

        call.enqueue(object : retrofit2.Callback<String> {
            override fun onResponse(
                call: retrofit2.Call<String>,
                response: retrofit2.Response<String>
            ) {
                val res = response.body().toString()
                val gson = GsonBuilder().create()
                val stop2 = gson.fromJson(res, Stops2::class.java)
                println("HELLLOOOO")
                runOnUiThread() {
                    addMarkers2(stop2)
                }
            }

            override fun onFailure(call: retrofit2.Call<String>, t: Throwable) {
                println("failed")
            }
        })
        // Data send
/*
        val call: retrofit2.Call<Stops2> = service.sentStopData("stops {\ngtfsId\nname\nlat\nlon\nzoneId\n  }")

        call.enqueue(object : retrofit2.Callback<Stops2> {
            override fun onResponse(
                call: retrofit2.Call<Stops2>,
                response: retrofit2.Response<Stops2>?
            ) {
                val res = response?.body().toString()
                val gson = GsonBuilder().create()
                val stop2 = gson.fromJson(res, Stops2::class.java)

                runOnUiThread(){
                    addMarkers2(stop2)
                }
            }

            override fun onFailure(call: retrofit2.Call<Stops2>, t: Throwable) {
                println("failed")
            }
        })*/
    }

    fun addMarkers(stop: Stops) {
        for (i in stop.features) {
            if (i.attributes.REI_VOIM == 1 && i.attributes.AIK_VOIM == 1) {
                val loc = LatLng(i.geometry.y, i.geometry.x)
                mMap.addMarker(
                    MarkerOptions().position(loc).title(i.attributes.SOLMUTUNNU).snippet(
                        i.attributes.REI_VOIM.toString()
                    )
                )
            }
        }
    }

    fun addMarkers2(stop: Stops2) {
        val loc = LatLng(stop.lat, stop.lon)
        mMap.addMarker(MarkerOptions().position(loc).title(stop.name))
    }
}

class Stops(val features: List<Stop>)
class Stop(val attributes: Attributes, val geometry: Geometry)
class Attributes(val SOLMUTUNNU: String, val NIMI1: String, val REI_VOIM: Int, val AIK_VOIM: Int)
class Geometry(val x: Double, val y: Double)

class Stops2(
    val name: String,
    val lat: Double,
    val lon: Double
)
