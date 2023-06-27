package com.dicoding.dicodingstories.ui.maps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.dicodingstories.R
import com.dicoding.dicodingstories.data.remote.ListStoryItem
import com.dicoding.dicodingstories.data.user.LoginPreference
import com.dicoding.dicodingstories.data.user.LoginViewModelFactory
import com.dicoding.dicodingstories.databinding.ActivityMapsBinding
import com.dicoding.dicodingstories.ui.login.LoginActivity
import com.dicoding.dicodingstories.ui.main.MainActivity
import com.dicoding.dicodingstories.ui.main.dataStore
import com.dicoding.dicodingstories.ui.post.PostActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsViewModel: MapsViewModel
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Stories Map"

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupViewModel()
        bottomNav()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mapsViewModel.listLocPost.observe(this){setupMap(it)}

        getMyLocation()

    }

    private fun setupViewModel() {
        mapsViewModel = ViewModelProvider(
            this, LoginViewModelFactory(LoginPreference.getInstance(dataStore))
        )[MapsViewModel::class.java]

        mapsViewModel.snackbarError.observe(this){
            it.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(window.decorView.rootView,snackBarText, Snackbar.LENGTH_SHORT).setTextMaxLines(5).show()
            }
        }

        mapsViewModel.isLogin().observe(this) { login ->
            if (login.token.isEmpty()) {
                startActivity(Intent(this@MapsActivity, LoginActivity::class.java))
                finish()
            } else {
                mapsViewModel.getAllLocPost(login.token)
            }
        }
    }

    private fun setupMap(item: List<ListStoryItem>?){
        item!!.forEach {story ->
            val latLong = LatLng(story.lat!!.toString().toDouble(), story.lon!!.toString().toDouble())
            mMap.addMarker(MarkerOptions()
                .position(latLong)
                .title(story.name)
                .snippet(story.description))
            boundsBuilder.include(latLong)
        }

        val bounds: LatLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300
            )
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun bottomNav(){
        binding.navView.selectedItemId = R.id.bnmMap
        binding.navView.isItemHorizontalTranslationEnabled = true
        binding.navView.setOnNavigationItemSelectedListener {item ->
            when(item.itemId){
                R.id.bnmHome -> {
                    val intent = Intent(this@MapsActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.bnmAddStory -> {
                    val intent = Intent(this@MapsActivity, PostActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    finish()
                    true
                }
                else -> true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.settingLogout -> {
                AlertDialog.Builder(this).apply {
                    setTitle("Logout Dicoding Stories")
                    setMessage("Apakah anda yakin ingin logout dari aplikasi? Anda bisa masuk kembali kapan saja degan akun yang terdaftar.")
                    setPositiveButton("Keluar"){_, _->
                        mapsViewModel.logout()
                    }
                    create()
                    show()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

}