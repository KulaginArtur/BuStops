package com.example.bustops

import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint


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
                val msg = "latitude: ${task.result!!.latitude} and longitude: ${task.result!!.longitude}"
                main_textview.text = msg
                map.setTileSource(TileSourceFactory.MAPNIK)
                //map.setBuiltInZoomControls(true)
                map.setMultiTouchControls(true)
                map.controller.setZoom(17.0)
                map.controller.setCenter(GeoPoint(task.result!!.latitude, task.result!!.longitude))

            }
        }

    }
}


