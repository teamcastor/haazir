package com.teamcastor.haazir

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.teamcastor.haazir.databinding.FragmentAttendanceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AttendanceFragment : Fragment() {
    private lateinit var ctx: Context
    private lateinit var binding: FragmentAttendanceBinding
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var previewView: PreviewView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }
    private fun isPortraitMode(): Boolean {
        return getApplicationContext(ctx).resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Image Capture
            val imageCapture = ImageCapture.Builder()
                .build()

            // Image Analysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                // Unfortunately this seems to cap out at device resolution
                .setTargetResolution(Size(1080,1920))
                .build()

            // Preview
            val preview = Preview.Builder()
                .setTargetResolution(Size(720,1280))
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .addUseCase(imageCapture)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val faceDetectOps = FaceDetectorOptions.Builder()
                    .setMinFaceSize(0.2F)
                    .build()

                val faceDetector = FaceDetection.getClient(faceDetectOps)
                val image = InputImage.fromMediaImage(imageProxy.image!!, rotationDegrees)
                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        graphicOverlay.clear()
                        for (face in faces) {
                            face.trackingId
                            graphicOverlay.add(
                                FaceGraphic(
                                    graphicOverlay,
                                    face
                                )
                            )
                            if (isPortraitMode()) {
                                graphicOverlay.setImageSourceInfo(image.height, image.width, true)
                            } else
                                graphicOverlay.setImageSourceInfo(image.width, image.height, true)

                            fun Bitmap.rotate(degrees: Float): Bitmap =
                                Bitmap.createBitmap(
                                    this,
                                    0,
                                    0,
                                    width,
                                    height,
                                    Matrix().apply { postRotate(degrees) },
                                    true
                                )

                            val bitmap = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                            // ImageAnalysis usecase gives YUV_420_8888 format images
                            // Convert them to ARGB_8888 bitmap
                            val yuvToRgbConverter = Utils.YuvToRgbConverter(ctx)
                            yuvToRgbConverter.yuvToRgb(imageProxy.image!!, bitmap!!)
                            val rotatedBitmap = bitmap.rotate(rotationDegrees.toFloat())
                            // The coordinates of this are according to ImageAnalysis imageProxy
                            // GraphicOverlay will transform them to preview size
                            val rect = Rect(face.boundingBox)

                            // Confirm the face is in the view fram
                            if (rotatedBitmap.width != 0 && rotatedBitmap.height != 0) {
                                if (rect.left < 0 || rect.top < 0 || rect.left + rect.width() > (rotatedBitmap.width) ||
                                    rect.top + rect.height() > rotatedBitmap.height
                                ) {
                                    Log.i("TAG", "Face is not in the frame")

                                } else {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        val faceBitmap = Utils.cropFace(rect, rotatedBitmap)
                                        Utils.saveImage(faceBitmap)
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
                    .addOnFailureListener { e -> }
                    .addOnCompleteListener {imageProxy.close()}
            }


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, useCaseGroup)

            } catch(exc: Exception) {
                Log.e("AF", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(ctx))

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
            startCamera()

    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
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
