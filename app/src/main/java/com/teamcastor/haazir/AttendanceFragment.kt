package com.teamcastor.haazir

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
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
import com.teamcastor.haazir.databinding.FragmentAttendanceBinding
import kotlinx.coroutines.*
import java.lang.Thread.sleep

class AttendanceFragment : Fragment() {
    private lateinit var ctx: Context
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private lateinit var binding: FragmentAttendanceBinding
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var previewView: PreviewView
    private var hasFace = false
    private lateinit var cameraXSource: CameraXSource
    private var lensFacing = CameraSourceConfig.CAMERA_FACING_FRONT
    private var bitmap: Bitmap? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    private fun startCameraXSource() {
        val faceDetector = FaceDetection.getClient()
        val detectionTaskCallback = DetectionTaskCallback { detectionTask ->
            detectionTask
                .addOnSuccessListener { onFaceDetectionSuccess(it) }
                .addOnFailureListener { e -> /* Do nothing */ }
        }
        val builder = CameraSourceConfig.Builder(ctx, faceDetector, detectionTaskCallback)
            .setFacing(lensFacing)
            // CameraX wil decide this according to device capabilites
            .setRequestedPreviewSize(1080,1920)
        cameraXSource = CameraXSource(builder.build(), previewView)
        cameraXSource.start()
        needUpdateGraphicOverlayImageSourceInfo = true

    }

    private fun onFaceDetectionSuccess(faces: List<Face>) {
        graphicOverlay.clear()
        if (faces.isNotEmpty()) {
            val size: Size = cameraXSource.previewSize!!
            hasFace = true
            graphicOverlay.setImageSourceInfo(size.height, size.width, true)
            needUpdateGraphicOverlayImageSourceInfo = false
            // TODO: The quality is not satisfactory
            bitmap = previewView.bitmap
            if (bitmap != null) {
                for (face in faces) {
                    graphicOverlay.add(FaceGraphic(graphicOverlay, face, bitmap))
                    // Confirm we have bounding box of face
                    rect?.let {
                        val faceBitmap: Bitmap = Utils.cropFace(it, bitmap!!)
                        // TODO: This is NOT OK. This should be replaced with coroutineScope
                        GlobalScope.launch {
                            val fas = FaceAntiSpoofing(ctx)
                            val fasl = FaceAntiSpoofing.laplacian(faceBitmap)
                            println("Laplacian: $fasl")
                            if (fasl > FaceAntiSpoofing.LAPLACE_FINAL_THRESHOLD) {
                                val score = fas.antiSpoofing(faceBitmap)
                                if (score < FaceAntiSpoofing.SPOOF_THRESHOLD) {
                                    FaceGraphic.selectedColor = Color.GREEN
                                } else
                                    FaceGraphic.selectedColor = Color.RED
                            }
                            else
                                FaceGraphic.selectedColor = Color.YELLOW
                            fas.close()
                        }
                    }
                }
            }
        }
        else if (faces.isEmpty() and hasFace) {
            hasFace = false
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request camera permissions
        requestPermission(binding.root)
        if (allPermissionsGranted())
            startCameraXSource()

    }

    override fun onStop() {
        super.onStop()
        if (::cameraXSource.isInitialized) {
            cameraXSource.stop()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::cameraXSource.isInitialized) {
            cameraXSource.stop()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::cameraXSource.isInitialized) {
            cameraXSource.start()
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
                Utils.showSnackbar(
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
        var rect: Rect? = null

    }
}
