package com.example.bustops

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.Marker
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var  arFragment : ArFragment
    lateinit var fragment: com.google.ar.sceneform.ux.ArFragment
    lateinit var  settingsFragment : SettingsFragment
    lateinit var  homeFragment : HomeFragment


    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
        //loadData()
        navListener()
        setUpPermissions()

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

    fun saveData(list: MutableList<Marker>) {
        val string = "shared preferences"
        val sharedPreference = MainActivity().getSharedPreferences(string, MODE_PRIVATE) ?: return
        val editor = sharedPreference.edit()

        fun set(key:String, value:String?) {
            editor.putString(key, value).apply()
        }
        fun <T> setList(key:String, list:List<T>) {
            val gson = GsonBuilder().create()
            val json = gson.toJson(list)
            set(key, json)
        }
        setList("Markers", list)
    }

    private fun loadData() {
        val sharedPreference = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val gson = GsonBuilder().create()
        val json = sharedPreference.getString("Markers", null)
        val type = object: TypeToken<ArrayList<Marker>>() {}.type
        settingsFragment.markers = gson.fromJson(json,type)

    }

    private fun navListener() {
        btm_nav.setOnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home -> {
                homeFragment = HomeFragment(this)
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, homeFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
                return@setOnNavigationItemSelectedListener true
            }

            R.id.ar -> {
                arFragment = ArFragment()
                supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(arFragment.toString())
                    .replace(R.id.frame_layout, arFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
                return@setOnNavigationItemSelectedListener true
            }
            R.id.settings -> {
                settingsFragment = SettingsFragment(this)
                supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(homeFragment.toString())
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
}
