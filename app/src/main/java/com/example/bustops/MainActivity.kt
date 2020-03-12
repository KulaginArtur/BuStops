package com.example.bustops

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    lateinit var  arFragment : ArFragment
    var  settingsFragment = SettingsFragment()
    lateinit var homeFragment : HomeFragment

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val REQUEST_CHECK_SETTINGS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        homeFragment = HomeFragment(this)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, homeFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        navListener()
        setUpPermissions()
        //fetchJson()

       // supportFragmentManager.beginTransaction().add(R.id.home, homeFragment).commit()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                Log.d("Locationupdate", "${p0.lastLocation}")
                //placeMarkerOnMap(p0.lastLocation)
                Log.d("Locationupdate", "still alive?")
                //addMarkers(p0.lastLocation)//LatLng(lastLocation.latitude, lastLocation.longitude))
                Log.d("Locationupdate", "why place marker is no called?")

            }
        }
        createLocationRequest()
    }

    private fun navListener() {
        btm_nav.setOnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home -> {
                Log.d("FRAG", "AR")
                homeFragment = HomeFragment(this)
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, homeFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
                return@setOnNavigationItemSelectedListener true
            }

            R.id.ar -> {
                Log.d("FRAG", "AR")
                arFragment = ArFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, arFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
                return@setOnNavigationItemSelectedListener true
            }
            R.id.settings -> {
                settingsFragment = SettingsFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, settingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
                return@setOnNavigationItemSelectedListener true
            }
        }
        false
    }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    private fun setUpPermissions() {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10 * 10000
        locationRequest.fastestInterval = 3 * 30000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Check your location settings first, if location is enabled
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Catches if there's any errors and ignores it
                }
            }
        }
    }
/*
    fun fetchJson() {

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
                val stop = gson.fromJson(body, Stops2::class.java)

                val jsonLeir1:  String = applicationContext.assets.open("leir1.json").bufferedReader().use { it.readText() }
                val jsonLeir2:  String = applicationContext.assets.open("leir2.json").bufferedReader().use { it.readText() }
                val jsonHonk:   String = applicationContext.assets.open("honk.json" ).bufferedReader().use { it.readText() }
                val jsonRaap:   String = applicationContext.assets.open("raap.json" ).bufferedReader().use { it.readText() }

                val routesLeir1 = gson.fromJson(jsonLeir1, TimeT::class.java)
                val routesLeir2 = gson.fromJson(jsonLeir2, TimeT::class.java)
                val routesHonk = gson.fromJson(jsonHonk, TimeT::class.java)
                val routesRaap = gson.fromJson(jsonRaap, TimeT::class.java)

                val stop1 = Stops("Leiritie",         routesLeir1, 60.258942, 24.846895)
                val stop2 = Stops("Leiritie",         routesLeir2, 60.25919 , 24.84605 )
                val stop3 = Stops("Honkasuo",         routesHonk , 60.258935, 24.843145)
                val stop4 = Stops("Raappavuorentie",  routesRaap , 60.25945 , 24.84224 )

                runOnUiThread {
                    addMarkersHSL(stop)
                    addMarkersLoc(stop1)
                    addMarkersLoc(stop2)
                    addMarkersLoc(stop3)
                    addMarkersLoc(stop4)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }
        })
    }

    fun addMarkersHSL(stop: Stops2) {
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
        for (i in stop.features) {

                val loc = LatLng(i.geometry.y, i.geometry.x)
                mMap.addMarker(
                    MarkerOptions().position(loc).title(i.attributes.SOLMUTUNNU).snippet(i.attributes.NIMI1)
                )

        }
    }

     fun addMarkersLoc(stop: Stops) {

        val current = LocalDateTime.now()
        val hour = DateTimeFormatter.ofPattern("HH")
        val mint = DateTimeFormatter.ofPattern("mm")
        var formattedH = current.format(hour).toInt()
        var formattedM = current.format(mint).toInt()

        val routes = stop.timeT.routes
        val loc = LatLng(stop.x, stop.y)

        // Using Custom Info Window
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
        var count = 0
        var snippetText = ""

        do {
            for (i in routes) {
                val diff = i.min - formattedM
                if (i.h == formattedH && i.min >= formattedM) {
                    snippetText += diff.toString() + "min" + "  " + i.route + "\n"
                    count += 1
                }
            }
            if (count <= 4) {
                formattedH += 1
                formattedM -= 60
                if (formattedH == 24) {
                    formattedH = 10
                }
            }
        } while (snippetText.length < 50)

        mMap.addMarker(
            MarkerOptions().position(loc).title(stop.NIMI1 + "  200m").snippet(snippetText)
        )
    }*/
}
