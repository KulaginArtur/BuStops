package com.example.bustops

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class HomeFragment(context: Context) : Fragment(), OnMapReadyCallback,
    GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private var cntx = context
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(cntx)
        mapLocation()
        fetchJson()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isBuildingsEnabled = true
        mMap.setOnInfoWindowClickListener(this)
    }

    override fun onInfoWindowClick(marker: Marker) {
        Toast.makeText(
            cntx, "Added to Favorites",
            Toast.LENGTH_SHORT
        ).show()
        /*SettingsFragment(cntx).markers.add(marker)

        MainActivity().saveData(marker)*/
    }

    private fun mapLocation() {

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {

        /*Haversine algorithm to calculate distance */

        val dlon = Math.toRadians(lon2 - lon1)
        val dlat = Math.toRadians(lat2 - lat1)
        val a = (sin(dlat / 2) * sin(dlat / 2)) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dlat / 2) * sin(dlon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // Radius of earth in meters
        val earthRadiusInM = 6371 * 1000

        // calculate the result
        return c * earthRadiusInM


    }

    // Gets stops from HSL and 4 Locally next to school
    private fun fetchJson() {

        val url =
            "https://services1.arcgis.com/sswNXkUiRoWtrx0t/arcgis/rest/services/HSL_pysakit_kevat2018/FeatureServer/0/query?where=1%3D1&outFields=*&outSR=4326&f=json"

        val request = Request
            .Builder()
            .url(url)
            .build()

        val client = OkHttpClient() // Using OkHttp to make a call to the HSL URL
        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = GsonBuilder().create()
                val stop = gson.fromJson(body, Stops2::class.java) // Take JSON and make Java Class


                // Finding local files where stop times are stored
                val jsonLeir1: String =
                    cntx.applicationContext.assets.open("leir1.json").bufferedReader()
                        .use { it.readText() }
                val jsonLeir2: String =
                    cntx.applicationContext.assets.open("leir2.json").bufferedReader()
                        .use { it.readText() }
                val jsonHonk: String =
                    cntx.applicationContext.assets.open("honk.json").bufferedReader()
                        .use { it.readText() }
                val jsonRaap: String =
                    cntx.applicationContext.assets.open("raap.json").bufferedReader()
                        .use { it.readText() }


                // From JSON to Java Class TimeT to use in stops
                val routesLeir1 = gson.fromJson(jsonLeir1,  TimeT::class.java)
                val routesLeir2 = gson.fromJson(jsonLeir2,  TimeT::class.java)
                val routesHonk  = gson.fromJson(jsonHonk,   TimeT::class.java)
                val routesRaap  = gson.fromJson(jsonRaap,   TimeT::class.java)


                //Making four stops next to the school with time tables from above
                val leiri1  = Stops("Leiritie",        routesLeir1,  60.258942, 24.846895, true)
                val leiri2  = Stops("Leiritie",        routesLeir2,  60.25919,  24.84605, false)
                val honk    = Stops("Honkasuo",        routesHonk,   60.258935, 24.843145, true)
                val raap    = Stops("Raappavuorentie", routesRaap,   60.25945,  24.84224, false)


                // Used to display stops on the map
                activity?.runOnUiThread {
                    addMarkersHSL(stop)
                    addMarkersLoc(leiri1)
                    addMarkersLoc(leiri2)
                    addMarkersLoc(honk)
                    addMarkersLoc(raap)

                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }
        })
    }

    fun addMarkersHSL(stop: Stops2) { // Adding stops from HSL
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(cntx))
        for (i in stop.features) {
            if (i.attributes.REI_VOIM == 1 && i.attributes.AIK_VOIM == 1) {
                val loc = LatLng(i.geometry.y, i.geometry.x)
                mMap.addMarker(
                    MarkerOptions().position(loc).title(i.attributes.SOLMUTUNNU)
                        .snippet(i.attributes.NIMI1)
                )
            }
        }
    }

    private fun addMarkersLoc(stop: Stops) { // Adding local stops

        val current= LocalDateTime.now()
        val hour= DateTimeFormatter.ofPattern("HH")
        val mint= DateTimeFormatter.ofPattern("mm")
        var formattedH      = current.format(hour).toInt()
        var formattedM      = current.format(mint).toInt()

        val routes = stop.timeT.routes

        val loc = LatLng(stop.x, stop.y)

        // Using Custom Info Window
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(cntx))
        var count = 0
        var snippetText = ""
            // Do while there is 5 rows of routes
        do {
            for (i in routes) {
                val diff = i.min - formattedM // Getting the time difference
                if (i.h == formattedH && i.min >= formattedM) { // Next routes and stop times
                    snippetText += diff.toString() + "min" + "  " + i.route + "\n"
                    count += 1
                }
            }
            if (count <= 4) {// if only 4 or less routes see first in next hour
                formattedH += 1
                formattedM -= 60
                if (formattedH == 24) { // if no busses today go to tomorow
                    formattedH = 0
                }
            }
        } while (snippetText.length < 50)

        mMap.addMarker( // add markers, title, distance and next routes
            MarkerOptions().position(loc).title(stop.NIMI1 + " " + distance(lastLocation.latitude, lastLocation.longitude, stop.x, stop.y).toInt() + "m").snippet(snippetText)
        )
    }
}

// Class for Fetched JSON from HSL  https://public-transport-hslhrt.opendata.arcgis.com/datasets/hsln-pysäkit
class Stops2(val features: List<Stop>)
class Stop(val attributes: Attributes, val geometry: Geometry)
class Attributes(
    val SOLMUTUNNU: String,
    val NIMI1: String,
    val REI_VOIM: Int,
    val AIK_VOIM: Int
)

class Geometry(val x: Double, val y: Double)

// Class for local JSON Routes
class Stops(
    val NIMI1: String,
    val timeT: TimeT,
    val x: Double,
    val y: Double,
    val selected: Boolean
)

class TimeT(val routes: List<Json>)

class Json(
    val route: String,
    val h: Int,
    val min: Int
)

