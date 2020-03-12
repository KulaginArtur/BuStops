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
import kotlinx.android.synthetic.main.activity_main.*

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

    /*override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpMap()

    }*/

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
       /* mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isBuildingsEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }
    }*/

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
    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {

        /*Haversine algorithm to calculate distance */

        val dlon = Math.toRadians(lon2 - lon1)
        val dlat = Math.toRadians(lat2 - lat1)
        val a = (Math.sin(dlat / 2) * Math.sin(dlat / 2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dlat / 2) * Math.sin(dlon/ 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        Log.d("MATH", "MAth calc")

        // Radius of earth in meters
        val earthRadiusInM = 6371*100

        // calculate the result
        return c * earthRadiusInM
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
                val stop = gson.fromJson(body, Stops2::class.java)

                val stop1  = Stops2( "Leiritie", 60.258942,24.846895)
                val stop2  = Stops2( "Leiritie",  60.25919,24.84605)
                val stop3  = Stops2( "Honkasuo", 60.258935,24.843145)
                val stop4  = Stops2( "Raappavuorentie", 60.25945,24.84224)

                runOnUiThread(){
                    addMarkers2(stop1)
                    addMarkers2(stop2)
                    addMarkers2(stop3)
                    addMarkers2(stop4)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }
        })
    }

    fun addMarkers(stop: Stops) {
        for (i in stop.features) {
            if (i.attributes.REI_VOIM == 1 && i.attributes.AIK_VOIM == 1) {
                println("found name: ${i.attributes.NIMI1}")
                val loc = LatLng(i.geometry.x, i.geometry.y)
                mMap.addMarker(
                    MarkerOptions().position(loc).title(i.attributes.SOLMUTUNNU).snippet(
                        i.attributes.NIMI1
                    )
                )
            }
        }
    }

    fun addMarkers2(stop: Stops2) {
        val loc = LatLng(stop.x, stop.y)

        // This function calculates the distance
        var calcDistance = distance(lastLocation.latitude, lastLocation.longitude, stop.x, stop.y)
        mMap.addMarker(
            MarkerOptions().position(loc).title(stop.NIMI1).snippet("Distance: ${calcDistance.toShort()}m")
        )
    }


    class Stops(val features: List<Stop>)
    class Stop(val attributes: Attributes, val geometry: Geometry)
    class Attributes(
        val SOLMUTUNNU: String,
        val NIMI1: String,
        val REI_VOIM: Int,
        val AIK_VOIM: Int
    )

    class Geometry(val x: Double, val y: Double)

    class Stops2(
        val NIMI1: String,
        val x: Double,
        val y: Double
    ) */
}
