package com.example.bustops

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment(context: Context) : Fragment(), OnMapReadyCallback {

    var mainActivity = MainActivity
    private lateinit var mMap: GoogleMap
    private var cntx = context
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    var currentl = LatLng(60.2585857, 24.8433926)
    /*private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false */

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
        //addMarkersHSL(stop)

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
        val a = (Math.sin(dlat / 2) * Math.sin(dlat / 2))
        +Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dlat / 2) * Math.sin(
            dlon / 2
        )
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        Log.d("MATH", "MAth calc")

        // Radius of earth in meters
        val earthRadiusInM = 6371 * 100

        // calculate the result
        return c * earthRadiusInM
    }

    fun addMarkers2(stop: Stops3) {
        val loc = LatLng(stop.x, stop.y)

        // This function calculates the distance
        val calcDistance = distance(currentl.latitude, currentl.longitude, stop.x, stop.y)
        mMap.addMarker(
            MarkerOptions().position(loc).title(stop.NIMI1)
                .snippet("Distance: ${calcDistance.toShort()}m")
        )
    }

    val stop1 =
        Stops3("Leiritie", 60.258942, 24.846895)
    val stop2 =
        Stops3("Leiritie", 60.25919, 24.84605)
    val stop3 =
        Stops3("Honkasuo", 60.258935, 24.843145)
    val stop4 =
        Stops3("Raappavuorentie", 60.25945, 24.84224)

    class Stops3(
        val NIMI1: String,
        val x: Double,
        val y: Double
    )
    class Stops2(val features: List<Stop>)
    class Stop(val attributes: Attributes, val geometry: Geometry)
    class Attributes(
        val SOLMUTUNNU: String,
        val NIMI1: String
    )

    class Geometry(val x: Double, val y: Double)

    class Stops(
        val NIMI1: String,
        val timeT: TimeT,
        val x: Double,
        val y: Double
    )

    class TimeT(val routes: List<Json>)

    class Json(
        val route: String,
        val h: Int,
        val min: Int
    )

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

                val jsonLeir1:  String = cntx.applicationContext.assets.open("leir1.json").bufferedReader().use { it.readText() }
                val jsonLeir2:  String = cntx.applicationContext.assets.open("leir2.json").bufferedReader().use { it.readText() }
                val jsonHonk:   String = cntx.applicationContext.assets.open("honk.json" ).bufferedReader().use { it.readText() }
                val jsonRaap:   String = cntx.applicationContext.assets.open("raap.json" ).bufferedReader().use { it.readText() }

                val routesLeir1 = gson.fromJson(jsonLeir1, TimeT::class.java)
                val routesLeir2 = gson.fromJson(jsonLeir2, TimeT::class.java)
                val routesHonk = gson.fromJson(jsonHonk, TimeT::class.java)
                val routesRaap = gson.fromJson(jsonRaap, TimeT::class.java)

                val leiri1  = Stops("Leiritie",         routesLeir1, 60.258942, 24.846895)
                val leiri2  = Stops("Leiritie",         routesLeir2, 60.25919 , 24.84605 )
                val honk    = Stops("Honkasuo",         routesHonk , 60.258935, 24.843145)
                val raap    = Stops("Raappavuorentie",  routesRaap , 60.25945 , 24.84224 )

                activity?.runOnUiThread {
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

    fun addMarkersHSL(stop: Stops2) {
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(cntx))
        for (i in stop.features) {

            val loc = LatLng(i.geometry.y, i.geometry.x)
            mMap.addMarker(
                MarkerOptions().position(loc).title(i.attributes.SOLMUTUNNU)
                    .snippet(i.attributes.NIMI1)
            )

        }
    }

    private fun addMarkersLoc(stop: Stops) {

        val current = LocalDateTime.now()
        val hour = DateTimeFormatter.ofPattern("HH")
        val mint = DateTimeFormatter.ofPattern("mm")
        var formattedH = current.format(hour).toInt()
        var formattedM = current.format(mint).toInt()

        val routes = stop.timeT.routes
        val loc = LatLng(stop.x, stop.y)

        // Using Custom Info Window
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(cntx))
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
    }
}


