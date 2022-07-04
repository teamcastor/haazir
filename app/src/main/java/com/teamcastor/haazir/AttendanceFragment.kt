package com.teamcastor.haazir

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
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

class AttendanceFragment : Fragment() {
    private lateinit var ctx: Context
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private lateinit var binding: FragmentAttendanceBinding
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var previewView: PreviewView
    private var hasFace = false
    private lateinit var cameraXSource: CameraXSource
    private var lensFacing = CameraSourceConfig.CAMERA_FACING_FRONT

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    private fun startCameraXSource() {
        val faceDetector = FaceDetection.getClient()
        val detectionTaskCallback = DetectionTaskCallback<List<Face>> { detectionTask ->
            detectionTask
                .addOnSuccessListener { onFaceDetectionSuccess(it) }
                .addOnFailureListener { e -> /* Do nothing */ }
        }
        val builder = CameraSourceConfig.Builder(ctx, faceDetector, detectionTaskCallback)
            .setFacing(lensFacing)
            .setRequestedPreviewSize(720,960)
        cameraXSource = CameraXSource(builder.build(), previewView)
        cameraXSource.start()
        needUpdateGraphicOverlayImageSourceInfo = true

    }

    private fun onFaceDetectionSuccess(faces: List<Face>) {
        graphicOverlay.clear()
        if (faces.isNotEmpty()) {
            val fullImage: Bitmap? = previewView.bitmap
            val size: Size = cameraXSource.previewSize!!
            hasFace = true
            graphicOverlay.setImageSourceInfo(size.height, size.width, true)
            needUpdateGraphicOverlayImageSourceInfo = false
            for (face in faces) {
                graphicOverlay.add(FaceGraphic(graphicOverlay, face, fullImage))
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
        cameraXSource.stop()
    }

    override fun onPause() {
        super.onPause()
        cameraXSource.stop()
    }

    override fun onResume() {
        super.onResume()
        cameraXSource.start()
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

    }
}
