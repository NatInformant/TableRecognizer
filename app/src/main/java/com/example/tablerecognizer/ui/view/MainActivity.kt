package com.example.tablerecognizer.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.tablerecognizer.App
import com.example.tablerecognizer.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    private var density: Float? = null
    private val applicationComponent
        get() = App.getInstance().applicationComponent
    private var isSnackBarDismissed: Int? = 0
    private val viewModel: MainViewModel by viewModels { applicationComponent.getMainViewModelFactory() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        isSnackBarDismissed = savedInstanceState?.getInt("isSnackBarDismissed")

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        viewModel.message.observe(this) {
            showMessage(it)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        density = resources.displayMetrics.density
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    private fun showMessage(newMessage: String) {
        if (newMessage == "" || isSnackBarDismissed == 1) {
            return
        }

        val snackbar =
            Snackbar.make(viewBinding.frameLayout, newMessage, Snackbar.LENGTH_INDEFINITE)

        snackbar.setAction("OK") { // User clicked OK button
            snackbar.dismiss()
            isSnackBarDismissed = 1
        }

        val view = snackbar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackbar.show()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        val outputDirectory = getExternalFilesDir(null)
        if (outputDirectory != null) {
            val photoFile = File(outputDirectory, "photo_" + System.currentTimeMillis() + ".jpg")
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            // Set up image capture listener, which is triggered after photo has
            // been taken
            imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val ei = ExifInterface(photoFile.absolutePath)
                        val orientation: Int =
                            ei.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                        val startBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                        val currentBitmap: Bitmap = when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> startBitmap.rotate(90F)
                            ExifInterface.ORIENTATION_ROTATE_180 -> startBitmap.rotate(180F)
                            ExifInterface.ORIENTATION_ROTATE_270 -> startBitmap.rotate(270F)
                            else -> startBitmap
                        }


                        val screenWidth = viewBinding.viewFinder.width
                        val screenHeight = viewBinding.viewFinder.height

                        val scaleFactorX = currentBitmap.width.toFloat() / screenWidth
                        val scaleFactorY = currentBitmap.height.toFloat() / screenHeight

                        val resultBitmap = Bitmap.createBitmap(
                            currentBitmap,
                            (viewBinding.cropFrame.x * scaleFactorX).toInt(),
                            (viewBinding.cropFrame.y * scaleFactorY).toInt(),
                            (viewBinding.cropFrame.width * scaleFactorX).toInt(),
                            (viewBinding.cropFrame.height * scaleFactorY).toInt()
                        )

                        viewModel.sendPhoto(resultBitmap)
                        isSnackBarDismissed=2
                    }
                }
            )
        }
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        val rotatedImg =
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        this.recycle()
        return rotatedImg
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            // Preview
            val preview = Preview.Builder().apply {
                this.setTargetResolution(Size(screenWidth, screenHeight))
            }
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            imageCapture =
                ImageCapture.Builder()
                    .setTargetResolution(Size(screenWidth, screenHeight))
                    .setTargetRotation(windowManager.defaultDisplay.rotation)
                    .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("isSnackBarDismissed", isSnackBarDismissed?:0)
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}