package com.dicoding.dicodingstories.ui.post

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstories.R
import com.dicoding.dicodingstories.data.user.LoginPreference
import com.dicoding.dicodingstories.data.user.LoginViewModelFactory
import com.dicoding.dicodingstories.databinding.ActivityPostBinding
import com.dicoding.dicodingstories.ui.camera.CameraActivity
import com.dicoding.dicodingstories.ui.main.MainActivity
import com.dicoding.dicodingstories.ui.main.dataStore
import com.dicoding.dicodingstories.ui.maps.MapsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PostActivity : AppCompatActivity() {

    private lateinit var postViewModel: PostViewModel
    private lateinit var postBinding : ActivityPostBinding
    private val timeStamp: String = SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(System.currentTimeMillis())
    private var getFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object{
        const val CAMERA_X_RESULT = 200
        private const val MAXIMAL_SIZE = 1000000

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        postBinding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(postBinding.root)
        setupViewModel()
        supportActionBar?.title = "Share Your Story"

        if (!allPermissionsGranted()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        postBinding.btnPostCamera.setOnClickListener{getStories()}
        postBinding.btnPostGalery.setOnClickListener{toGallery()}
        postBinding.btnPostShare.setOnClickListener{uploadImage()}
        bottomNav()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun setupViewModel() {
        postViewModel = ViewModelProvider(
            this,LoginViewModelFactory(LoginPreference.getInstance(dataStore))
        )[PostViewModel::class.java]

        postBinding.progressBar.visibility = View.GONE
        postViewModel.isLoading.observe(this){showLoading(it)}
        postViewModel.snackbarError.observe(this){
            it.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(window.decorView.rootView,snackBarText, Snackbar.LENGTH_SHORT).setTextMaxLines(5).show()
            }
        }
        postViewModel.isError.observe(this){successPost(it)}
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Sayang, Anda belum memeperbolehkan kami mengakses kamera.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getStories() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as? File

            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            myFile?.let { file ->
                getFile = file
                rotateFile(file, isBackCamera)
                postBinding.ivPostPreview.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    uploadImage()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    uploadImage()
                }
                else -> {
                    // No location access granted.
                }
            }
        }


    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun rotateFile(file: File, isBackCamera: Boolean = false) {
        val matrix = Matrix()
        val bitmap = BitmapFactory.decodeFile(file.path)
        val rotation = if (isBackCamera) 90f else -90f
        matrix.postRotate(rotation)
        if (!isBackCamera) {
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > MAXIMAL_SIZE)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@PostActivity)
                getFile = myFile
                postBinding.ivPostPreview.setImageURI(uri)
            }
        }
    }

    private fun toGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadImage() {
        when {
            getFile == null -> {
                Toast.makeText(this@PostActivity, "Gambar tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            }
            postBinding.edtPostDescription.text.toString().isEmpty() ->{
                Toast.makeText(this@PostActivity, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            postBinding.cbUseLoc.isChecked -> {
                if     (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                ) { fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {

                        val file = reduceFileImage(getFile as File)
                        val description = postBinding.edtPostDescription.text.toString().toRequestBody("text/plain".toMediaType())
                        val lat = location.latitude.toString().toRequestBody("text/plain".toMediaType())
                        val long = location.longitude.toString().toRequestBody("text/plain".toMediaType())
                        val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
                        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                            "photo",
                            file.name,
                            requestImageFile
                        )

                        postViewModel.isLogin().observe(this){login ->
                            postViewModel.shareStories(login.token, imageMultipart, description, lat, long)
                        }

                    } else {
                        Toast.makeText(
                            this@PostActivity,
                            "Location is not found. Try Again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                } else {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }



            else-> {
                val file = reduceFileImage(getFile as File)


                val description = postBinding.edtPostDescription.text.toString().toRequestBody("text/plain".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )

                postViewModel.isLogin().observe(this){login ->
                    postViewModel.shareStories(login.token, imageMultipart, description, null, null)
                }

            }
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
                        postViewModel.logout()
                    }
                    create()
                    show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bottomNav(){
        postBinding.navView.selectedItemId = R.id.bnmAddStory
        postBinding.navView.isItemHorizontalTranslationEnabled = true
        postBinding.navView.setOnNavigationItemSelectedListener {item ->
            when(item.itemId){
                R.id.bnmHome -> {
                    val intent = Intent(this@PostActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    finish()
                }

                R.id.bnmMap -> {
                    val intent = Intent(this@PostActivity, MapsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            postBinding.progressBar.visibility = View.VISIBLE
        } else {
            postBinding.progressBar.visibility = View.GONE
        }
    }

    private fun successPost(isError : Boolean) {
        if (!isError){
            val intent = Intent(this@PostActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            finish()
        }
    }



}