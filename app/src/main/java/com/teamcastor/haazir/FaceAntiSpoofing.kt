package com.teamcastor.haazir
import android.content.Context
import android.graphics.*
import android.util.Log
import com.teamcastor.haazir.ml.FaceAntiSpoofing
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.absoluteValue


class FaceAntiSpoofing(context: Context) {
    // Try to use GPU
    private val compatList = CompatibilityList()
    private val options: Model.Options = if (compatList.isDelegateSupportedOnThisDevice) {
        Model.Options.Builder().setDevice(Model.Device.GPU).build()
    } else {
        Model.Options.Builder().setNumThreads(4).build()
    }
    private val model = FaceAntiSpoofing.newInstance(context)

    fun antiSpoofing(bitmap: Bitmap): Float{
        val current = System.currentTimeMillis()
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(convertBitmapToByteBuffer(bitmap))
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val outputFeature1 = outputs.outputFeature1AsTensorBuffer
        val end = System.currentTimeMillis()
        Log.v("FAS", "Time-taken for model inference : ${end - current}")
        return leafScore(outputFeature0, outputFeature1)
    }

    private fun leafScore(out1: TensorBuffer, out2: TensorBuffer): Float {
        var score = 0.0F
        for (i in 0..7) {
            score += ((out1.floatArray[i]).absoluteValue * out2.floatArray[i])
        }
        println("Score: $score")
        return score
    }

    fun close() {
        model.close()
    }

    companion object {
        
        const val SPOOF_THRESHOLD = 0.2
        const val LAPLACE_THRESHOLD = 50
        const val LAPLACE_FINAL_THRESHOLD = 550

        fun convertBitmapToByteBuffer(bp: Bitmap) : ByteBuffer {
            val current = System.currentTimeMillis()
            val imageBuffer = ByteBuffer.allocate(256*256*3*4)
            imageBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(256 * 256)
            bp.getPixels(intValues, 0, bp.width, 0, 0, bp.width, bp.height)
            // Convert the image to floating point.
            for (pixel in 0..(65535)) {                                    //
                    val value = intValues[pixel]
                    // Normalize to [-1.0,1.0]
                    imageBuffer.putFloat((value shr 16 and 0xFF) / 255.0f) // Red
                    imageBuffer.putFloat((value shr 8 and 0xFF) / 255.0f)  // Green
                    imageBuffer.putFloat((value and 0xFF) / 255.0f)        // Blue
                }
            val end = System.currentTimeMillis()
            Log.v("FAS", "Time-taken for convertBitmapTBB : ${end - current}")
            return imageBuffer
        }

        fun laplacian(bitmap: Bitmap): Int {
            val laplace = arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, -4, 1), intArrayOf(0, 1, 0))
            val size: Int = laplace.size
            val img: Array<IntArray> = convertGreyImg(bitmap)
            val height = img.size
            val width: Int = img[0].size
            var score = 0
            for (x in 0 until height - size + 1) {
                for (y in 0 until width - size + 1) {
                    var result = 0
                    for (i in 0 until size) {
                        for (j in 0 until size) {
                            result += (img[x + i][y + j] and 0xFF) * laplace[i][j]
                        }
                    }
                    if (result > LAPLACE_THRESHOLD) {
                        score++
                    }
                }
            }
            return score
        }

        private fun convertGreyImg(bitmap: Bitmap): Array<IntArray> {
            val w = bitmap.width
            val h = bitmap.height
            val pixels = IntArray(h * w)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            val result = Array(h) {
                IntArray(
                    w
                )
            }
            val alpha = 0xFF shl 24
            for (i in 0 until h) {
                for (j in 0 until w) {
                    val `val` = pixels[w * i + j]
                    val red = `val` shr 16 and 0xFF
                    val green = `val` shr 8 and 0xFF
                    val blue = `val` and 0xFF
                    var grey =
                        (red.toFloat() * 0.3 + green.toFloat() * 0.59 + blue.toFloat() * 0.11).toInt()
                    grey = alpha or (grey shl 16) or (grey shl 8) or grey
                    result[i][j] = grey
                }
            }
            return result
        }
    }
}