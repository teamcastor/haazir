package com.teamcastor.haazir

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.teamcastor.haazir.data.model.AppViewModel
import com.teamcastor.haazir.databinding.FragmentAttendanceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AttendanceFragment : Fragment() {
    private lateinit var ctx: Context
    private var _binding: FragmentAttendanceBinding? = null
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var previewView: PreviewView
    private lateinit var resultView: TextView
    private val binding get() = _binding!!
    private var camera: Camera? = null
    private val appViewModel: AppViewModel by activityViewModels()



    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

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
            var isRecognized = false

            fun reset() {
                isSharp = false
                isNotSpoof = false
                isRecognized = false
            }

            val faceDetectOps = FaceDetectorOptions.Builder()
                .setMinFaceSize(0.2F)
                .build()

            val faceDetector = FaceDetection.getClient(faceDetectOps)

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->

                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                val image = InputImage.fromMediaImage(imageProxy.image!!, rotationDegrees)

                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        lifecycleScope.launch {
                            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                                graphicOverlay.clear()
                                if (faces.isNotEmpty()) {
                                    val face = faces.first()
                                     graphicOverlay.setImageSourceInfo(image.height, image.width, true)
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
                                    graphicOverlay.add(
                                        FaceGraphic(
                                            graphicOverlay,
                                            face,
                                            rotatedBitmap
                                        )
                                    )
                                    // The coordinates of this are according to ImageAnalysis imageProxy
                                    // GraphicOverlay will transform them to preview size
                                    val rect = Rect(face.boundingBox)
                                    // 20% padding
                                    rect.scale(1.20F)
                                    // Confirm the face is in the view fram
                                    if (rotatedBitmap.width != 0 && rotatedBitmap.height != 0) {
                                        if (rect.left < 0 || rect.top < 0 || rect.left + rect.width() > (rotatedBitmap.width) ||
                                            rect.top + rect.height() > rotatedBitmap.height
                                        ) {
                                            binding.helpText.text =
                                                "Detected face is outside camera bounds.\n" +
                                                        "Please bring it in the center of the preview"
                                            Log.i("AttendanceFragment", "Face is not in the frame")

                                        } else {
                                            binding.helpText.text = "Face Detected. Processing"
                                            binding.processingBar.visibility = View.VISIBLE
                                            val faceBitmap =
                                                Utils.cropFace(rect, rotatedBitmap, 128, 128)
                                            val fasl = AntiSpoofing.laplacian(faceBitmap)
                                            if (fasl > AntiSpoofing.LAPLACE_FINAL_THRESHOLD) {
                                                isSharp = true
                                                val spoofingJob = launch {
                                                    isNotSpoof = runAntiSpoofing(faceBitmap)
                                                }
                                                val recognitionJob = launch {
                                                    val faceBitmap2 =
                                                        Utils.getResizedBitmap(faceBitmap, 112, 112)
                                                    val output = runRecognition(faceBitmap2)
                                                    val vectorFromDb = appViewModel.user.value?.vector?.toFloatArray()
                                                    val score = async {
                                                        FaceRecognition.distance(
                                                            output,
                                                            vectorFromDb!!
                                                        )
                                                    }
                                                    val score2 = async {
                                                        FaceRecognition.cosineSimilarity(
                                                            output,
                                                            vectorFromDb!!
                                                        )
                                                    }
                                                    isRecognized =
                                                        (score.await() < FaceRecognition.EUCLIDEAN_THRESHOLD
                                                                && score2.await() > FaceRecognition.COSINE_THRESHOLD)
                                                }
//                                              Wait for jobs to finish
                                                spoofingJob.join()
                                                recognitionJob.join()

                                            }
                                            println("isSharp: $isSharp, isRecognized: $isRecognized, isNotSpoof: $isNotSpoof")
                                            if (isSharp && isRecognized && isNotSpoof) {
                                                reset()
                                                // This will automatically happen when navigation happens, but in case that takes time, we
                                                // can do it beforehand this way.
                                                // lifecycleScope.cancel()
                                                // Do we really need to check this? I guess there's no harm.
                                                if (findNavController().currentDestination?.id == R.id.AttendanceFragment) {
                                                    findNavController().navigate(R.id.action_Attendance_to_PostAttendanceFragment)
                                                }
                                                appViewModel.markAttendance()
                                            }
                                        }
                                }
                                }
                                else {
                                    binding.processingBar.visibility = View.INVISIBLE
                                    binding.helpText.text = "No face detected"
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
        _binding = FragmentAttendanceBinding.inflate(
            inflater,
            container,
            false
        )
        previewView = binding.viewFinder
        graphicOverlay = binding.graphicOverlay
        resultView = binding.resultInfo

        binding.topbar.toolbar.apply {
            setupWithNavController(findNavController())
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}