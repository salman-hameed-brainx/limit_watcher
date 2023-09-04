package com.brainx.limitwatcher

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brainx.limitwatcher.databinding.ActivityMainBinding
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.speedlimit.api.MapboxSpeedLimitApi
import com.mapbox.navigation.ui.speedlimit.model.SpeedLimitFormatter
import java.lang.Math.round


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 101
    lateinit var mapView: MapView
    private val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var mapboxNavigation: MapboxNavigation
    var isKeepScreenOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapView = MapView(
            this, MapInitOptions(
                this, ResourceOptions.Builder().accessToken(getString(R.string.token)).build()
            )
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toggleScreenOn.setOnClickListener {
            isKeepScreenOn = !isKeepScreenOn
            binding.toggleScreenOn.text = if (!isKeepScreenOn) {
                // If the toggle button is checked, keep the screen on
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                "Turn Screen Off"
            } else {
                // If the toggle button is unchecked, clear the FLAG_KEEP_SCREEN_ON flag
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                "Turn Screen On"
            }
        }

        mapView.location.apply {
            locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@MainActivity, R.drawable.navigation_arrow
                )
            )
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

        mapView.attribution.enabled = false
        mapView.logo.updateSettings {
            enabled = false
        }
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        binding.mapView.addView(mapView)
    }

    override fun onResume() {
        super.onResume()

        isKeepScreenOn =
            window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0
        binding.toggleScreenOn.text = if (isKeepScreenOn) {
            // If the toggle button is checked, keep the screen on
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            "Turn Screen Off"
        } else {
            // If the toggle button is unchecked, clear the FLAG_KEEP_SCREEN_ON flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            "Turn Screen On"
        }
        if (!isLocationEnabled()) {
            showLocationServicesAlertDialog()
        } else {
            // Check and request location permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // Permission is already granted. Initialize your map and show the user's location here.
                // You can use the LocationComponent to display the user's location on the map.
                // Add your map initialization logic here.
                InitMap()
            }
        }

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showLocationServicesAlertDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Location Services Disabled")
        alertDialog.setMessage("Please enable location services to use this app.")
        alertDialog.setPositiveButton("Enable") { _, _ ->
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, 1211)
        }
        alertDialog.setNegativeButton("Cancel") { dialog: DialogInterface?, _: Int ->
            dialog?.dismiss()
            // Handle the scenario where the user cancels the dialogue (optional).
        }
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1211) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // Permission is already granted. Initialize your map and show the user's location here.
                // You can use the LocationComponent to display the user's location on the map.
                // Add your map initialization logic here.
                InitMap()
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. Initialize your map and show the user's location here.
                // You can use the LocationComponent to display the user's location on the map.
                InitMap()
            } else {
                // Permission denied. Handle this case if needed (e.g., show a message).
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Location Permission Denied")
        alertDialog.setMessage("Please grant location permission to use this app.")
        alertDialog.setPositiveButton("OK") { dialog: DialogInterface?, _: Int ->
            dialog?.dismiss()
            // Handle the scenario where the user denies permission (optional).
        }
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun InitMap() {
        // initialize Mapbox Navigation
        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this).accessToken(getString(R.string.token)).build()
            )
        }

        mapboxNavigation.apply {
            startTripSession()
            registerLocationObserver(locationObserver)
        }
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    // Define speed limit formatter options
    private val speedLimitFormatter: SpeedLimitFormatter by lazy {
        SpeedLimitFormatter(this)
    }

    // Create an instance of the Speed Limit API
    private val speedLimitApi: MapboxSpeedLimitApi by lazy {
        MapboxSpeedLimitApi(speedLimitFormatter)
    }

    private val locationObserver = object : LocationObserver {

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )

            // Invoke this method to move the camera to your current location as the route progresses.
            updateCamera(
                Point.fromLngLat(
                    enhancedLocation.longitude, enhancedLocation.latitude
                ), enhancedLocation.bearing.toDouble()
            )


            val speed = locationMatcherResult.enhancedLocation.speed.toDouble() * 3.6
            val roundSpeed: Int = round(speed).toInt()
            val speedlimit = speedLimitApi.updateSpeedLimit(locationMatcherResult.speedLimit)

            Log.d("speed_double", "" + speed.toString())
            Log.d("speed_int", "" + roundSpeed.toString())
            Log.d("speed_limit", "" + speedlimit.value?.speedKPH.toString())

            binding.tvCurrentSpeedLimit.text = if (speedlimit.value?.speedKPH != null)
                speedlimit.value?.speedKPH.toString()
            else
                "N/A"

            binding.tvCurrentSpeed.text = roundSpeed.toString()
        }

        override fun onNewRawLocation(rawLocation: android.location.Location) {

        }
    }


    private fun updateCamera(point: Point, bearing: Double? = null) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        mapView.camera.easeTo(
            CameraOptions.Builder()
                // Centers the camera to the lng/lat specified.
                .center(point)
                // specifies the zoom value. Increase or decrease to zoom in or zoom out
                .zoom(17.0)
                // adjusts the bearing of the camera measured in degrees from true north
                .bearing(bearing)
                // adjusts the pitch towards the horizon
                .pitch(45.0)
                // specify frame of reference from the center.
                .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0)).build(), mapAnimationOptions
        )
    }
}