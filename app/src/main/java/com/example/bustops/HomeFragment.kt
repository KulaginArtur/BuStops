package com.example.bustops

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment(context: Context): Fragment(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    private var cntx = context
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    var currentl = LatLng(60.2585857,24.8433926)
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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isBuildingsEnabled = true
        addMarkers2(stop1)
        addMarkers2(stop2)
        addMarkers2(stop3)
        addMarkers2(stop4)
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
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dlat / 2) * Math.sin(dlon/ 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        Log.d("MATH", "MAth calc")

        // Radius of earth in meters
        val earthRadiusInM = 6371*100

        // calculate the result
        return c * earthRadiusInM
    }




   /* private fun fetchJson() {
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

                    addMarkers2(stop1)
                    addMarkers2(stop2)
                    addMarkers2(stop3)
                    addMarkers2(stop4)

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
    } */

    fun addMarkers2(stop: Stops2) {
        val loc = LatLng(stop.x, stop.y)

        // This function calculates the distance
        val calcDistance = distance(currentl.latitude, currentl.longitude, stop.x, stop.y)
        mMap.addMarker(
            MarkerOptions().position(loc).title(stop.NIMI1).snippet("Distance: ${calcDistance.toShort()}m")
        )
    }

    val stop1  =
        Stops2("Leiritie", 60.258942, 24.846895)
    val stop2  =
        Stops2("Leiritie", 60.25919, 24.84605)
    val stop3  =
        Stops2("Honkasuo", 60.258935, 24.843145)
    val stop4  = Stops2(
        "Raappavuorentie",
        60.25945,
        24.84224
    )


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
    )
}
