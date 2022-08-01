package com.teamcastor.haazir

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.teamcastor.haazir.data.model.LoginViewModel
import com.teamcastor.haazir.databinding.FragmentAttendanceBinding
import kotlinx.coroutines.*


class AttendanceFragment : Fragment() {
    private lateinit var ctx: Context
    private var _binding: FragmentAttendanceBinding? = null
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var previewView: PreviewView
    private lateinit var resultView: TextView
    private val binding get() = _binding!!
    private val parentJob = Job()
    private var camera: Camera? = null
    private val loginViewModel: LoginViewModel by activityViewModels()



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

            // Image Analysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(view!!.display.rotation)
                .setTargetResolution(Size(720,1280))
                .build()

            // Preview
            val preview = Preview.Builder()
                .setTargetResolution(Size(720,1280))
                .setTargetRotation(view!!.display.rotation)
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .build()
            val coroutineScope = CoroutineScope(Dispatchers.Default + parentJob)
            var recognitionJob: Job = Job()
            var spoofJob: Job = Job()

            var isSharp = false
            var isSpoof = true
            var isRecognized = false

            fun reset() {
                isSharp = false
                isSpoof = true
                isRecognized = false
            }

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val faceDetectOps = FaceDetectorOptions.Builder()
                    .setMinFaceSize(0.2F)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .build()

                val faceDetector = FaceDetection.getClient(faceDetectOps)
                val image = InputImage.fromMediaImage(imageProxy.image!!, rotationDegrees)


                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isEmpty()) {
                            reset()
                            parentJob.cancelChildren()
                        }
                        graphicOverlay.clear()
                        if (faces.size == 1) {
                            val face = faces.first()
                            if (isPortraitMode()) {
                                graphicOverlay.setImageSourceInfo(image.height, image.width, true)
                            } else
                                graphicOverlay.setImageSourceInfo(image.width, image.height, true)

                            val bitmap = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888,
                            )

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
                            // Confirm the face is in the view fram
                            if (rotatedBitmap.width != 0 && rotatedBitmap.height != 0) {
                                if (rect.left < 0 || rect.top < 0 || rect.left + rect.width() > (rotatedBitmap.width) ||
                                    rect.top + rect.height() > rotatedBitmap.height
                                ) {
                                    Log.i("AttendanceFragment", "Face is not in the frame")

                                }
                                else {
                                    val faceBitmap = Utils.cropFace(rect, rotatedBitmap, 256)
                                    val fasl = FaceAntiSpoofing.laplacian(faceBitmap)
                                    if (fasl > FaceAntiSpoofing.LAPLACE_FINAL_THRESHOLD) {
                                        isSharp = true
                                        coroutineScope.launch {

                                            spoofJob = launch {
                                                val result = runAntiSpoofing(faceBitmap)
                                                isSpoof = (result > FaceAntiSpoofing.SPOOF_THRESHOLD)

                                            }
                                            recognitionJob = launch {
                                                val faceBitmap2 = Utils.getResizedBitmap(faceBitmap, 112, 112)
                                                val output = runRecognition(faceBitmap2)
                                                val floatArray = FloatArray(myemb.size)
                                                for (i in myemb.indices) {
                                                    floatArray[i] = myemb[i].toFloat()
                                                }
                                                val score = async {FaceRecognition.distance(output, floatArray)}
                                                val score2 = async {FaceRecognition.cosineSimilarity(output, floatArray)}
                                                isRecognized = (score.await() < FaceRecognition.EUCLIDEAN_THRESHOLD
                                                        && score2.await() > FaceRecognition.COSINE_THRESHOLD)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e -> }
                    .addOnCompleteListener {
                        loginViewModel.authenticate(isSharp, isSpoof, isRecognized)
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
                    this, cameraSelector, useCaseGroup)

            } catch(exc: Exception) {
                Log.e("AF", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(ctx))

    }
    private suspend fun runAntiSpoofing(bitmap: Bitmap) =
        withContext(Dispatchers.Default) {
            val fas = FaceAntiSpoofing(ctx)
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

        // Request camera permissions
        requestPermission(binding.root)
        if (allPermissionsGranted()) {
            startCamera()
        }

        loginViewModel.scanResult.observe(viewLifecycleOwner) {
            resultView.text = "isSharp : ${it.isSharp}\n" +
                    "isSpoof : ${it.isSpoof}\n" +
                    "isRecognized : ${it.isRecognized}"
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        // clean up coroutine job
        parentJob.cancel()
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
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        val myemb = doubleArrayOf(0.0025200418, 0.0155994715, 0.0045897807, -0.0013550388, -0.016698375, 1.5892272E-4, 0.044154435, 0.13986644, 0.032934055, -0.03751144, 0.020804804, 0.0039824923, 0.0069982796, 0.013789998, -0.0017237498, -0.18309548, -0.011988789, 0.017813804, 0.005035952, -0.007076773, 0.12268059, 0.05687858, -0.092539236, 0.0012218071, 0.09405953, 0.014103971, -0.012649783, 0.069206126, 0.023597505, -0.13517338, 0.004019673, 0.21363343, 0.05446595, -0.0025820099, 0.19076303, -0.13226502, 0.0545651, 0.0059613446, -0.0051805447, -0.018111251, -0.0072998586, -7.492993E-4, -0.0054779924, 0.013426452, -0.0013416124, 0.0069900174, -0.14832717, -0.024638573, 0.0019251467, 0.0051557575, -0.11144368, -0.0038854086, -0.09412562, 1.08250766E-4, 0.12360598, 0.024374174, -0.1942002, -1.8319361E-4, -0.015946494, 0.0023403338, 0.051590625, 0.015401173, -0.032603554, -0.021383174, 0.0031149369, -0.18428528, -0.0024931887, 0.0067999815, 0.008725128, 0.003835834, -0.002898048, -0.2770889, 0.07469238, -0.0042592837, 0.013178579, 0.014946739, -0.0026171254, 0.010451976, -0.11712823, -0.0052135945, 0.0073576956, 0.062133487, 4.970369E-4, -0.05317701, 0.04108081, 0.0013922198, 0.008609454, 0.1583743, 0.13371919, -0.06471137, 0.22394495, 0.005998526, 0.0014304335, 0.0028773919, -0.07812955, -0.039296128, -0.073238194, -0.056911632, -4.5882317E-4, 0.032372206, -0.0010260908, 0.0040134764, -0.0076220934, 0.0011660357, -0.003645798, 8.2056277E-4, -0.169479, 0.00812197, -5.2518083E-4, 0.008997789, -0.12023491, -0.0064736153, 0.013699112, 0.27021456, 0.008741653, 0.13081083, -2.1817985E-4, 0.0046558804, 0.057506524, -0.051821973, -0.0483848, 0.025861412, 0.06966882, -0.0051227077, 9.4191724E-4, 0.008477255, 0.0018714408, -0.0049244096, 0.012624996, 0.15361513, -3.6819463E-4, 0.010526339, 0.004172528, -0.010798998, -0.054697298, 0.0016762408, -0.10364395, -0.017285008, -0.0056184535, -0.008390499, 0.0035693706, -0.003044706, -0.0033524816, -0.16445544, 0.1834921, -0.047161963, 0.017731179, -0.0011102643, -0.0013684653, -0.0030860184, 0.010625487, -0.14660859, 0.030190926, 0.0018290959, -0.0036623229, -0.0031438554, 0.004912016, -0.008592929, -0.010096692, -9.2177757E-4, -0.008535092, 0.0012631193, -5.985099E-4, -0.0023010874, 0.00855988, 0.0070891664, 0.00995623, -0.044683233, -0.0043501705, 0.0044327946, 0.054201555, 0.18679705, -0.0029310978, -0.07079252, 0.0057299966, -0.0014779425, -0.055986237, -0.048054304, -0.0050731334, 0.0038193092, -0.0018032758, -0.17251958, 0.004647618, -0.008683816, -0.14449342, -0.014062659, -0.1583743, 0.032372206, 0.115806244, 0.06880953, -0.026770279, -0.005717603)
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