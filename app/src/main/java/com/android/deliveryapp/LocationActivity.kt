package com.android.deliveryapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.deliveryapp.util.Keys.Companion.CLIENT
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.userType
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Client set his home location, or the Manager set his market location
 */
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val LOCATION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE)
        }

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val mapSettings = mMap.uiSettings
        mapSettings?.isZoomControlsEnabled = false
        mapSettings?.isZoomGesturesEnabled = true
        mapSettings?.isScrollGesturesEnabled = true
        mapSettings?.isTiltGesturesEnabled = true
        mapSettings?.isRotateGesturesEnabled = true

        val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

        if (sharedPreferences.getString(userType, null) == CLIENT) {

            val fusedLocationCLient = LocationServices.getFusedLocationProviderClient(this)

            fusedLocationCLient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val position = LatLng(location.latitude, location.longitude)

                        mMap.addMarker(MarkerOptions()
                            .position(position)
                            .title(getString(R.string.client_position))
                            .snippet(getString(R.string.client_pos_snippet)))
                    }
                }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Unable to show location - permission required",
                        Toast.LENGTH_LONG).show()
                } else {
                    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
            }
        }
    }

    /**
     * Request current location permission
     */
    private fun requestPermission(permissionType: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionType), requestCode)
    }
}