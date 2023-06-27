package com.dicoding.dicodingstories.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingstories.R
import com.dicoding.dicodingstories.data.helper.ViewModelFactory
import com.dicoding.dicodingstories.data.paging.LoadingPageAdapter
import com.dicoding.dicodingstories.data.paging.PostViewModelFactory
import com.dicoding.dicodingstories.data.remote.ListStoryItem
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import com.dicoding.dicodingstories.data.user.LoginViewModelFactory
import com.dicoding.dicodingstories.databinding.ActivityMainBinding
import com.dicoding.dicodingstories.ui.PostAdapter
import com.dicoding.dicodingstories.ui.login.LoginActivity
import com.dicoding.dicodingstories.ui.maps.MapsActivity
import com.dicoding.dicodingstories.ui.post.PostActivity
import com.google.android.material.snackbar.Snackbar

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var userViewModel : UserViewModel
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels {
        PostViewModelFactory(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Timeline Dicoding Stories"

        setupViewModel()
        bottomNav()
    }


    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,LoginViewModelFactory(LoginPreference.getInstance(dataStore))
        )[UserViewModel::class.java]

        userViewModel.isLogin().observe(this) { login ->
            if (login.token.isEmpty()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            } else {
                binding.progressBar.visibility = View.GONE
                val postAdapter = PostAdapter()
                mainViewModel.listLivePost.observe(this) {
                    postAdapter.submitData(lifecycle, it) }
                binding.rvPost.adapter = postAdapter.withLoadStateFooter(
                    LoadingPageAdapter {postAdapter.retry()}
                )
                binding.rvPost.layoutManager = LinearLayoutManager(this)

            }
        }
    }



    private fun bottomNav(){
        binding.navView.selectedItemId = R.id.bnmHome
        binding.navView.isItemHorizontalTranslationEnabled = true
        binding.navView.setOnNavigationItemSelectedListener {item ->
            when(item.itemId){
                R.id.bnmAddStory -> {
                    val intent = Intent(this@MainActivity, PostActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    finish()
                }

                R.id.bnmMap -> {
                    val intent = Intent(this@MainActivity, MapsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.logout_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.settingLogout -> {
                AlertDialog.Builder(this).apply {
                    setTitle("Logout Dicoding Stories")
                    setMessage("Apakah anda yakin ingin logout dari aplikasi? Anda bisa masuk kembali kapan saja degan akun yang terdaftar.")
                    setPositiveButton("Keluar"){_, _->
                        userViewModel.logout()
                    }
                    create()
                    show()
                }
            }

            R.id.bnmAddStory -> {
                startActivity(Intent(this@MainActivity, PostActivity::class.java))
            }

        }
        return super.onOptionsItemSelected(item)
    }

}