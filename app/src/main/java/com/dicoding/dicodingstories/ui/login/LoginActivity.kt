package com.dicoding.dicodingstories.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstories.R
import com.dicoding.dicodingstories.data.user.LoginPreference
import com.dicoding.dicodingstories.data.user.LoginViewModelFactory
import com.dicoding.dicodingstories.databinding.ActivityLoginBinding
import com.dicoding.dicodingstories.ui.main.MainActivity
import com.dicoding.dicodingstories.ui.register.RegisterActivity
import com.google.android.material.snackbar.Snackbar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {

    private lateinit var loginBinding : ActivityLoginBinding
    private lateinit var loginViewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        moveToRegister()
        setupViewModel()
        setupLogin()
        animateView()
        setButtonEnabled()
    }

    private fun setButtonEnabled() {
        loginBinding.edtLoginEmail.addTextChangedListener(
            object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    loginBinding.edtLoginPassword.addTextChangedListener(
                        object : TextWatcher{
                            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                            }

                            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                loginBinding.btnLogin.isEnabled = loginBinding.edtLoginPassword.error.isNullOrEmpty()
                            }

                            override fun afterTextChanged(p0: Editable?) {
                            }

                        }
                    )
                }
                override fun afterTextChanged(p0: Editable?) {
                    loginBinding.btnLogin.isEnabled = loginBinding.edtLoginEmail.error.isNullOrEmpty()
                            && loginBinding.edtLoginPassword.text.toString().isNotEmpty()
                }

            })

    }


    private fun animateView() {
        ObjectAnimator.ofFloat(loginBinding.ivLogin, View.TRANSLATION_X, -70f, 0f).apply {
            duration = 6000
        }.start()

        val titleLogin = ObjectAnimator.ofFloat(loginBinding.tvLoginTitle, View.ALPHA, 1f).setDuration(500)
        val titleEmail = ObjectAnimator.ofFloat(loginBinding.tvTitleEmail, View.ALPHA, 1f).setDuration(500)
        val edtEmail = ObjectAnimator.ofFloat(loginBinding.tilEmailLayout, View.ALPHA, 1f).setDuration(500)
        val titlePass = ObjectAnimator.ofFloat(loginBinding.tvTitlePassword, View.ALPHA, 1f).setDuration(500)
        val edtPass = ObjectAnimator.ofFloat(loginBinding.tilPasswordLayout, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(loginBinding.btnLogin, View.ALPHA, 1f).setDuration(500)
        val regis = ObjectAnimator.ofFloat(loginBinding.linearDidntRegister, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(titleLogin, titleEmail, edtEmail, titlePass, edtPass, login, regis)
            start()
        }

    }

    private fun successLogin(isError : Boolean) {
        if (!isError){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupLogin() {
        loginBinding.btnLogin.setOnClickListener{
            when {
                loginBinding.edtLoginEmail.text.toString().isEmpty() -> loginBinding.edtLoginEmail.error = "Email tidak boleh kosong"
                loginBinding.edtLoginPassword.text.toString().isEmpty() -> loginBinding.edtLoginPassword.error = "Password tidak boleh kosong"
                else -> {
                    val email = loginBinding.edtLoginEmail.text.toString()
                    val password = loginBinding.edtLoginPassword.text.toString()

                    loginViewModel.login(email, password)
                }
            }
        }

    }

    private fun setupViewModel() {
        loginViewModel = ViewModelProvider(this,
            LoginViewModelFactory(LoginPreference.getInstance(dataStore))
        )[LoginViewModel::class.java]

        loginViewModel.snackbarError.observe(this){ error ->
            error.getContentIfNotHandled()?.let {snack ->
                Snackbar.make(window.decorView.rootView, snack, Snackbar.LENGTH_SHORT).setTextMaxLines(5).show()
            }
        }
        loginBinding.progressBar.visibility = View.GONE
        loginViewModel.isLoading.observe(this){showLoading(it)}
        loginViewModel.isError.observe(this){successLogin(it)}
    }

    private fun moveToRegister() {
        loginBinding.tvRegister.setOnClickListener{
            val signupIntent = Intent(this@LoginActivity,RegisterActivity::class.java)
            signupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(signupIntent)
            finish()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loginBinding.progressBar.visibility = View.VISIBLE
        } else {
            loginBinding.progressBar.visibility = View.GONE
        }
    }
}