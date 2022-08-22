package com.teamcastor.haazir.ui.register

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenResumed
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.teamcastor.haazir.*
import com.teamcastor.haazir.R
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentRegisterCamBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.jar.Manifest


class RegisterCamFragment : Fragment() {
    private lateinit var ctx: Context
    private var _binding: FragmentRegisterCamBinding? = null
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var resultView: TextView
    private val binding get() = _binding!!
    private var camera: Camera? = null
    private lateinit var cameraProvider: ProcessCameraProvider
    private val appViewModel: AppViewModel by activityViewModels()
    private var isDialogVisibile = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    private fun showFullScreenDialog(bitmap: Bitmap, vector: FloatArray)
    {
        val view = layoutInflater.inflate(R.layout.fragment_image_preview_dialog, null)
        val imageView = view.findViewById<ImageView>(R.id.previewImage)
        imageView.setImageBitmap(bitmap)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Preview Image")
            .setView(view)
            .setPositiveButton("Proceed") { _, _ ->
                appViewModel.addVector(vector)
            }
            .setNegativeButton("Retry") { dialog, _ ->
                dialog.dismiss()
                isDialogVisibile = false
                onResume()
            }
            .setOnCancelListener {
                isDialogVisibile = false
                onResume()
            }
            .show()

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
//        if (ActivityCompat.checkSelfPermission(requireContext(),
//                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 1)
//        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Image Analysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(720,720))
                .build()

            // Preview
            val preview = Preview.Builder()
                .setTargetResolution(Size(360, 360))
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .build()

            var isSharp = false
            var isNotSpoof = false

            fun reset() {
                isSharp = false
                isNotSpoof = false
            }

            val faceDetectOps = FaceDetectorOptions.Builder()
                .setMinFaceSize(0.33F)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()

            val faceDetector = FaceDetection.getClient(faceDetectOps)

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->

                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                val image = InputImage.fromMediaImage(imageProxy.image!!, rotationDegrees)

                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        lifecycleScope.launch {
                            reset()
//                            graphicOverlay.clear()
                            if (faces.size == 1) {

                                val face = faces.first()
//                                graphicOverlay.setImageSourceInfo(image.height, image.width, true)

                                val bitmap = Bitmap.createBitmap(
                                    image.width,
                                    image.height,
                                    Bitmap.Config.ARGB_8888,
                                )

                                // ImageAnalysis usecase gives YUV_420_8888 format images
                                // Convert them to ARGB_8888 bitmap
                                val yuvToRgbConverter = Utils.YuvToRgbConverter(ctx)
                                yuvToRgbConverter.yuvToRgb(imageProxy.image!!, bitmap)
                                val rotatedBitmap = bitmap!!.rotate(rotationDegrees.toFloat())
//                                graphicOverlay.add(
//                                    FaceGraphic(
//                                        graphicOverlay,
//                                        face,
//                                        rotatedBitmap
//                                    )
//                                )
                                // The coordinates of this are according to ImageAnalysis imageProxy
                                // GraphicOverlay will transform them to preview size
                                val rect = Rect(face.boundingBox)
                                // Confirm the face is in the view fram
                                if (rotatedBitmap.width != 0 && rotatedBitmap.height != 0) {
                                    if (rect.left < 0 || rect.top < 0 || rect.left + rect.width() > (rotatedBitmap.width) ||
                                        rect.top + rect.height() > rotatedBitmap.height
                                    ) {
                                        whenCreated {
                                            if (binding != null) {
                                                binding.helpText.text =
                                                    "Detected face is outside camera bounds.\n" +
                                                            "Please bring it in the center of the preview"
                                                Log.i(
                                                    "AttendanceFragment",
                                                    "Face is not in the frame"
                                                )
                                            }
                                        }

                                    } else {
                                        whenCreated {
                                            binding.helpText.text = "Face Detected. Processing"
                                            binding.processingBar.visibility = View.VISIBLE
                                        }
                                        val faceBitmap =
                                            Utils.cropFace(rect, rotatedBitmap, 128, 128)
                                        val fasl = AntiSpoofing.laplacian(faceBitmap)
                                        if (fasl > AntiSpoofing.LAPLACE_FINAL_THRESHOLD) {
                                            isSharp = true
                                            val spoofingJob = launch {
                                                isNotSpoof = runAntiSpoofing(faceBitmap)
                                            }
//                                              Wait for jobs to finish
                                            spoofingJob.join()

                                        }
                                        println("isSharp: $isSharp, isNotSpoof: $isNotSpoof")
                                        if (isSharp && isNotSpoof) {
                                            val faceBitmap2 =
                                                Utils.getResizedBitmap(faceBitmap, 112, 112)
                                            val vector = runRecognition(faceBitmap2)
                                            if (!isDialogVisibile) {
                                                val previewBitmap =
                                                    Utils.cropFace(rect, rotatedBitmap)
                                                isDialogVisibile = true
                                                showFullScreenDialog(previewBitmap, vector)
                                                onPause()
                                            }
                                        }
                                    }
                                }
                            } else {
                                binding.processingBar.visibility = View.INVISIBLE
                                if (faces.isEmpty()) {
                                    binding.helpText.text = "No face detected"
                                } else {
                                    binding.helpText.text = "Detected ${faces.size} faces. Raavan?"
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e -> Log.w("", "", e) }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this as LifecycleOwner, cameraSelector, useCaseGroup
                )

            } catch(exc: Exception) {
                Log.e("AF", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(ctx))

    }

    private suspend fun runAntiSpoofing(bitmap: Bitmap) =
        withContext(Dispatchers.Default) {
            val fas = AntiSpoofing(ctx)
            val result = fas.antiSpoofing(bitmap)
            fas.close()
            return@withContext result

        }

    private suspend fun runRecognition(bitmap: Bitmap) =
        withContext(Dispatchers.Default) {
            val fr = FaceRecognition(ctx)
            val result = fr.getOutputVector(bitmap)
            fr.close()
            return@withContext result
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterCamBinding.inflate(
            inflater,
            container,
            false
        )
        graphicOverlay = binding.graphicOverlay
        resultView = binding.resultInfo

//        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 1)

        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 1)
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        cameraProvider.unbindAll()
    }

    override fun onResume() {
        super.onResume()
//        val permissionLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestPermission()) {
//            isGranted ->
//            if (isGranted) {
//
//            } else {
//                println("kise kamm da ni paaji")
//            }
//        }
//
//        permissionLauncher.launch(android.Manifest.permission.CAMERA)


        if (!isDialogVisibile) {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}