package com.teamcastor.haazir

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.teamcastor.haazir.ml.Mobilefacenet
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.pow
import kotlin.math.sqrt

class FaceRecognition(context: Context) {
    // This model doesn't work well on GPU for some reason
//    private val compatList = CompatibilityList()
//    private val options: Model.Options = if (compatList.isDelegateSupportedOnThisDevice) {
//        Model.Options.Builder().setDevice(Model.Device.GPU).build()
//    } else {
//        Model.Options.Builder().setNumThreads(4).build()
//    }
    private val model = Mobilefacenet.newInstance(context)

    fun getOutputVector(bitmap: Bitmap): FloatArray{
        val current = System.currentTimeMillis()
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 112, 112, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(Utils.convertBitmapToByteBuffer(bitmap, (112*112*3*4), 112,
            FloatArray(3) {127.5f}, FloatArray(3) {128f}))
        val outputs = model.process(inputFeature0)
        val outputFeature = outputs.outputFeature0AsTensorBuffer.floatArray
        val end = System.currentTimeMillis()
        Log.v("FAS", "Time-taken for model inference : ${end - current}")
        return outputFeature
    }
    

    fun close() {
        model.close()
    }
    companion object {
        // These are very generous thresholds
        const val COSINE_THRESHOLD = 0.65
        const val EUCLIDEAN_THRESHOLD = 0.85

        // Compute the cosine of the angle between x1 and x2.
        fun cosineSimilarity( x1 : FloatArray , x2 : FloatArray ) : Float {
            val mag1 = sqrt( x1.map { it * it }.sum() )
            val mag2 = sqrt( x2.map { it * it }.sum() )
            val dot = x1.mapIndexed{ i , xi -> xi * x2[ i ] }.sum()
            return dot / (mag1 * mag2)
        }
        // Compute the euclidean distance( L2 Norm) between emb1 and emb2
        fun distance(emb1: FloatArray, emb2: FloatArray): Float {
            var distance = 0f
            for (i in emb1.indices) {
                val diff = emb1[i] - emb2[i]
                distance += diff * diff
            }
            distance = Math.sqrt(distance.toDouble()).toFloat()
            return distance
        }
    }
}