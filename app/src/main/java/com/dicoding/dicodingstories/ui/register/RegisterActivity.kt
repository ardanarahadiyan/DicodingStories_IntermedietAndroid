package com.dicoding.dicodingstories.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.dicodingstories.R
import com.dicoding.dicodingstories.data.helper.ViewModelFactory
import com.dicoding.dicodingstories.databinding.ActivityRegisterBinding
import com.dicoding.dicodingstories.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {
    private lateinit var regBinding : ActivityRegisterBinding
    private val registerViewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()
        regBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(regBinding.root)

        setupViewModel()
        moveToLogin()
        register()
        animateView()
        setButtonEnabled()

    }

    private fun setButtonEnabled() {
        regBinding.edtRegisterEmail.addTextChangedListener(
            (object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    regBinding.edtRegisterPassword.addTextChangedListener(
                        (object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                            }
                            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                                regBinding.btnSignUp.isEnabled = regBinding.edtRegisterPassword.error.isNullOrEmpty()
                            }
                            override fun afterTextChanged(s: Editable) {
                            }
                        })
                    )
                }
                override fun afterTextChanged(s: Editable) {
                    regBinding.btnSignUp.isEnabled = regBinding.edtRegisterEmail.error.isNullOrEmpty()
                            && regBinding.edtRegisterPassword.text.toString().isNotEmpty()
                }
            })
        )
    }

    private fun animateView() {
        ObjectAnimator.ofFloat(regBinding.ivRegister, View.TRANSLATION_X, -70f, 0f).apply {
            duration = 6000
        }.start()

        val titleLogin = ObjectAnimator.ofFloat(regBinding.tvRegisterTitle, View.ALPHA, 1f).setDuration(500)
        val titleName = ObjectAnimator.ofFloat(regBinding.tvTitleRegisterName, View.ALPHA, 1f).setDuration(500)
        val edtName = ObjectAnimator.ofFloat(regBinding.tilRegisterNameLayout, View.ALPHA, 1f).setDuration(500)
        val titleEmail = ObjectAnimator.ofFloat(regBinding.tvTitleRegisterEmail, View.ALPHA, 1f).setDuration(500)
        val edtEmail = ObjectAnimator.ofFloat(regBinding.tilRegisterEmailLayout, View.ALPHA, 1f).setDuration(500)
        val titlePass = ObjectAnimator.ofFloat(regBinding.tvTitleRegisterPassword, View.ALPHA, 1f).setDuration(500)
        val edtPass = ObjectAnimator.ofFloat(regBinding.tilRegisterPasswordLayout, View.ALPHA, 1f).setDuration(500)
        val regis = ObjectAnimator.ofFloat(regBinding.btnSignUp, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(regBinding.linearRegistered, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(titleLogin, titleName,edtName,titleEmail, edtEmail, titlePass, edtPass, regis, login)
            start()
        }

    }

    private fun setupViewModel() {
        registerViewModel.snackbarError.observe(this){ error ->
            error.getContentIfNotHandled()?.let {snack ->
                Snackbar.make(window.decorView.rootView, snack, Snackbar.LENGTH_SHORT).setTextMaxLines(5).show()
            }
        }
        regBinding.progressBar.visibility = View.GONE
        registerViewModel.isLoading.observe(this){showLoading(it)}
        registerViewModel.isError.observe(this){showAlert(it)}
    }

    private fun register() {
        regBinding.btnSignUp.setOnClickListener {
            when {
                regBinding.edtRegisterName.text.toString().isEmpty() -> regBinding.edtRegisterName.error = "Nama tidak boleh kosong"
                regBinding.edtRegisterEmail.text.toString().isEmpty() -> regBinding.edtRegisterEmail.error = "Email tidak boleh kosong"
                regBinding.edtRegisterPassword.text.toString().isEmpty() -> regBinding.edtRegisterPassword.error = "Password tidak boleh kosong"
                else -> {
                    val name = regBinding.edtRegisterName.text.toString()
                    val email = regBinding.edtRegisterEmail.text.toString()
                    val password = regBinding.edtRegisterPassword.text.toString()

                    registerViewModel.register(name, email, password)
                }
            }
        }
    }

    private fun showAlert(isError: Boolean) {
        if (!isError) {
            AlertDialog.Builder(this).apply {
                setTitle("Berhasil Mendaftar!")
                setMessage("Selamat bergabung dengan Dicoding Stories. Akun anda berhasil didaftarkan. Silakan masuk dengan akun yang telah anda buat.")
                setPositiveButton("Masuk"){_, _->
                    val loginIntent = Intent(this@RegisterActivity,LoginActivity::class.java)
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(loginIntent)
                    finish()
                }
                create()
                show()
            }
        }
    }

    private fun moveToLogin(){
        regBinding.tvLogin.setOnClickListener {
                val loginIntent = Intent(this@RegisterActivity,LoginActivity::class.java)
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(loginIntent)
                finish()
        }

    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            regBinding.progressBar.visibility = View.VISIBLE
        } else {
            regBinding.progressBar.visibility = View.GONE
        }
    }
}