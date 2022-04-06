package com.example.customcamera.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.customcamera.R
import com.example.customcamera.customview.MyScanningView
import com.example.customcamera.databinding.ActivityMainBinding
import com.example.customcamera.extensions.MyExtensions.toast
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : BaseActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: Camera
    private var status: Boolean = false

    companion object {
        private const val TAG = "CameraXApplication"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreen(this)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        initViews()
    }

    private fun initViews() {

        // scan effect animation will start
        val myScanningView = findViewById<MyScanningView>(R.id.my_scanning_view)
        myScanningView.startAnimation()


        // Request camera permissions
        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

        viewBinding.lightButton.setOnClickListener {
            changeFlashLightState(!status)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun changeFlashLightState(status: Boolean) {
        camera.cameraControl.enableTorch(status) // or false
        this.status = status
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            // configure our MLKit BarcodeScanning client
            /* passing in our desired barcode formats - MLKit supports additional formats outside
             of the ones listed here, and you may not need to offer support for all of these.
             You should only specify the ones you need */
            val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_PDF417
            ).build()
            // getClient() creates a new instance of the MLKit barcode scanner with the specified options
            val scanner = BarcodeScanning.getClient(options)

            val imageAnalyzer = ImageAnalysis.Builder().build().also { imageAnalysis ->
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), { imageProxy ->
                    processImageProxy(scanner, imageProxy)
                })
            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera =
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

            } catch (illegalStateException: IllegalStateException) {
                // If the use case has already been bound to another lifecycle or method is not called on main thread.
                Log.e(TAG, illegalStateException.message.orEmpty())
            } catch (illegalArgumentException: IllegalArgumentException) {
                // If the provided camera selector is unable to resolve a camera to be used for the given use cases.
                Log.e(TAG, illegalArgumentException.message.orEmpty())
            } catch (exception: Exception) {
                Log.e(TAG, exception.message.orEmpty())
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        imageProxy.image.let { image ->
            val inputImage =
                InputImage.fromMediaImage(image!!, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(inputImage).addOnSuccessListener { barcodeList ->
                val barcode = barcodeList.getOrNull(0)
                // `rawValue` is the decoded value of the barcode
                barcode?.rawValue?.let { value ->

                    toast(value)
                    finish()

                    // show dialog
                    //  showDialog(value)
                }
            }
                .addOnFailureListener {
                    // This failure will happen if the barcode scanning model
                    // fails to download from Google Play Services
                    Log.e(TAG, it.message.orEmpty())
                }
                .addOnCompleteListener {
                    // When the image is from CameraX analysis use case, must
                    // call image.close() on received images when finished
                    // using them. Otherwise, new images may not be received
                    // or the camera may stall.
                    imageProxy.image?.close()
                    imageProxy.close()
                }
        }
    }

    private fun showDialog(message: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(resources.getString(R.string.barcode_value))
        alertDialog.setMessage(message)

        alertDialog.setPositiveButton(
            resources.getString(R.string.ok)
        ) { _, _ -> toast(resources.getString(R.string.ok)) }

        alertDialog.setNegativeButton(
            resources.getString(R.string.no)
        ) { _, _ -> toast(resources.getString(R.string.no)) }

        alertDialog.show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted())
                startCamera()
            else {
                toast(resources.getString(R.string.permissions_not_granted))
                finish()
            }
        }
    }
}

