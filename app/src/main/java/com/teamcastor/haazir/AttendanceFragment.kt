package com.teamcastor.haazir

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.camera.CameraSourceConfig
import com.google.mlkit.vision.camera.CameraXSource
import com.google.mlkit.vision.camera.DetectionTaskCallback
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.teamcastor.haazir.databinding.FragmentAttendanceBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import com.teamcastor.haazir.FaceGraphic.Companion


class AttendanceFragment : Fragment() {
    private lateinit var ctx: Context
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private lateinit var binding: FragmentAttendanceBinding
    private var graphicOverlay: GraphicOverlay? = null
    private var faceGraphic: FaceGraphic? = null
    private lateinit var previewView: PreviewView
    private var hasFace = false
    private var cameraXSource: CameraXSource? = null
    private var lensFacing = CameraSourceConfig.CAMERA_FACING_FRONT
    private var faceDetectorOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .build()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    @SuppressLint("MissingPermission")
    private fun startCameraXSource() {
        val faceDetector = FaceDetection.getClient(faceDetectorOpts)
        val detectionTaskCallback = DetectionTaskCallback<List<Face>> { detectionTask ->
            detectionTask
                .addOnSuccessListener { onFaceDetectionSuccess(it) }
                .addOnFailureListener { e -> /* Do nothing */ }
        }
        val builder = CameraSourceConfig.Builder(ctx, faceDetector, detectionTaskCallback)
            .setFacing(lensFacing)

        cameraXSource = CameraXSource(builder.build(), previewView)
        cameraXSource?.start()
        needUpdateGraphicOverlayImageSourceInfo = true
        showSnackbar(binding.root, "No Face Detected",
            Snackbar.LENGTH_INDEFINITE, null) {}

    }

    private fun onFaceDetectionSuccess(faces: List<Face>) {
        graphicOverlay!!.clear()
        if (faces.isNotEmpty()) {
            val size: Size = cameraXSource!!.previewSize!!
            val inputImage = previewView.bitmap
            hasFace = true
            showSnackbar(binding.root, "Face Detected",
                Snackbar.LENGTH_INDEFINITE, null) {}
            graphicOverlay!!.setImageSourceInfo(size.height, size.width, true)
            needUpdateGraphicOverlayImageSourceInfo = false
            for (face in faces) {
                graphicOverlay!!.add(FaceGraphic(graphicOverlay!!, face))
            }
        }
        else if (faces.isEmpty()) {
            hasFace = false
            showSnackbar(binding.root, "No Face Detected",
                Snackbar.LENGTH_INDEFINITE, null) {}

        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAttendanceBinding.inflate(
            inflater,
            container,
            false
        )
        previewView = binding.viewFinder
        graphicOverlay = binding.graphicOverlay
        return binding.root
    }

    private fun saveImage(bitmap: Bitmap?) {
        var outStream: FileOutputStream? = null
        // Write to SD Card
        try {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator +  "Haazir/")
            dir.mkdirs()
            val fileName = String.format("%s_%d.jpg", "Image", System.currentTimeMillis())
            val outFile = File(dir, fileName)
            outStream = FileOutputStream(outFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
            showSnackbar(binding.root, "Image Saved",Snackbar.LENGTH_SHORT, null) {}
        } catch (e: FileNotFoundException) {
            Log.i("FileNotFoundError",e.toString())
        } catch (e: IOException) {
            Log.i("IOException", e.toString())
        } finally {
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request camera permissions
        requestPermission(binding.root)
        if (allPermissionsGranted())
            startCameraXSource()

    }

    override fun onStop() {
        super.onStop()
        if (cameraXSource != null)
            cameraXSource?.stop()
    }

    override fun onPause() {
        super.onPause()
        if (cameraXSource != null)
            cameraXSource?.stop()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (cameraXSource != null)
            cameraXSource?.start()
    }

    private fun showSnackbar(
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(binding.root)
            }.show()
        } else {
            snackbar.show()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startCameraXSource()
                Log.i("Permission: ", "Granted")
            }
            else {
                requestPermission(binding.root)
                Log.i("Permission: ", "Denied")
            }
        }

    private fun requestPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {}

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            ) -> {
                showSnackbar(
                    view,
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}