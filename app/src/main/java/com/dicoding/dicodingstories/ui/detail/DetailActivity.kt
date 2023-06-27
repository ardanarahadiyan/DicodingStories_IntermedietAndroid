package com.dicoding.dicodingstories.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.dicodingstories.R
import com.dicoding.dicodingstories.data.remote.Story
import com.dicoding.dicodingstories.data.user.LoginPreference
import com.dicoding.dicodingstories.data.user.LoginViewModelFactory
import com.dicoding.dicodingstories.databinding.ActivityDetailBinding
import com.dicoding.dicodingstories.ui.login.LoginActivity
import com.dicoding.dicodingstories.ui.main.dataStore
import com.google.android.material.snackbar.Snackbar

class DetailActivity : AppCompatActivity() {

    private lateinit var detailViewModel: DetailViewModel
    private lateinit var detailBinding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        detailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)
        supportActionBar?.title = "Detail Story"

        setupViewModel()
    }

    private fun setupViewModel() {
        detailViewModel = ViewModelProvider(
            this, LoginViewModelFactory(LoginPreference.getInstance(dataStore))
        )[DetailViewModel::class.java]

        detailViewModel.isLogin().observe(this){login ->
            if (login.token.isEmpty()) {
                startActivity(Intent(this@DetailActivity, LoginActivity::class.java))
                finish()
            } else {
                detailViewModel.getSinglePost(login.token, intent.getStringExtra("StoryId").toString())
            }
        }
        detailViewModel.isLoading.observe(this){showLoading(it)}
        detailViewModel.snackbarError.observe(this){
            it.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(window.decorView.rootView,snackBarText, Snackbar.LENGTH_SHORT).setTextMaxLines(5).show()
            }
        }
        detailViewModel.singlePost.observe(this){showDetail(it)}
    }


    private fun showDetail(detail: Story?) {
        detailBinding.tvDetailName.text = detail?.name
        detailBinding.tvDetailDesc.text = detail?.description
        Glide.with(detailBinding.ivDetailPic)
            .load(detail?.photoUrl)
            .into(detailBinding.ivDetailPic)
        detailBinding.include.tvDetailLink.text = detail?.photoUrl
        detailBinding.include.tvDetailTime.text = detail?.createdAt
        detailBinding.include.tvDetailLat.text = detail?.lat.toString()
        detailBinding.include.tvDetailLon.text = detail?.lon.toString()
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
                        detailViewModel.logout()
                    }
                    create()
                    show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            detailBinding.progressBar.visibility = View.VISIBLE
        } else {
            detailBinding.progressBar.visibility = View.GONE
        }
    }



}