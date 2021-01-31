package com.android.deliveryapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.deliveryapp.databinding.ActivityLocationBinding
import com.android.deliveryapp.util.Keys.Companion.CLIENT
import com.android.deliveryapp.util.Keys.Companion.Lat
import com.android.deliveryapp.util.Keys.Companion.Lng
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.marketPosFirebase
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.userType
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException

/**
 * Client set his home location, or the Manager set his market location
 */
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityLocationBinding

    private val LOCATION_REQUEST_CODE = 101
    private val TAG = "GoogleMaps"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.title = getString(R.string.search)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.location_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchLocation -> {
                TODO("Appear edittext")
            }
            else -> super.onOptionsItemSelected(item)
        }
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

        database = FirebaseDatabase.getInstance()

        var geocoder: List<Address>? = null

        /**** CHECK PERMISSIONS *****/

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE)
        }

        /***** MAP SETTINGS *****/

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val mapSettings = mMap.uiSettings
        mapSettings?.isZoomControlsEnabled = false
        mapSettings?.isZoomGesturesEnabled = true
        mapSettings?.isScrollGesturesEnabled = true
        mapSettings?.isTiltGesturesEnabled = true
        mapSettings?.isRotateGesturesEnabled = true

        val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)

        val intent = Intent(this@LocationActivity, ProfileActivity::class.java)

        // get market position and show
        val marketRef = database.getReference(marketPosFirebase)

        var marketPosition: LatLng

        marketRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                marketPosition = LatLng(
                        snapshot.child(Lat).value as Double,
                        snapshot.child(Lng).value as Double)

                mMap.addMarker(MarkerOptions()
                        .position(marketPosition)
                        .title(getString(R.string.market_position))
                        .snippet(getString(R.string.market_snippet)))

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("REALTIME_DB", "Failed to read value.", error.toException())
            }
        })

        /*******************************  CLIENT ********************************/

        // if the user is client, map search for current location and set a marker
        if (sharedPreferences.getString(userType, null) == CLIENT) {

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val position = LatLng(location.latitude, location.longitude)

                        mMap.addMarker(MarkerOptions() // put marker
                            .position(position)
                            .title(getString(R.string.client_position))
                            .snippet(getString(R.string.client_pos_snippet))
                        )

                        // animate on current position
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12.0F))
                    }
                }

            binding.searchLocLayout.setEndIconOnClickListener { // if user press the search icon
                try {
                    geocoder = Geocoder(this).getFromLocationName(
                            binding.searchLocTextInput.text.toString(),
                            1
                    )
                } catch (e: IOException) {
                    Log.w(TAG, e.message.toString())
                }

                if (geocoder != null) {
                    mMap.addMarker(MarkerOptions()
                            .position(LatLng(geocoder!![0].latitude, geocoder!![0].longitude)))

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(
                            geocoder!![0].latitude,
                            geocoder!![0].longitude
                    ), 12.0F))
                }
            }
        }

        if (sharedPreferences.getString(userType, null) == MANAGER) {
            // TODO: 31/01/2021
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