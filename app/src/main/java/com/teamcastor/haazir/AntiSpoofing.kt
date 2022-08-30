package com.teamcastor.haazir

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.teamcastor.haazir.ml.AntiSpoofing
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class AntiSpoofing(context: Context) {

    private val options = Model.Options.Builder().setNumThreads(2).build()
    private val model = AntiSpoofing.newInstance(context, options)

    fun antiSpoofing(bitmap: Bitmap): Boolean {
        val current = System.currentTimeMillis()
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(
            Utils.convertBitmapToByteBuffer(
                bitmap,
                (128 * 128 * 3 * 4),
                128,
                MEAN,
                SCALE
            )
        )
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val end = System.currentTimeMillis()
        Log.v("FAS", "Time taken for FAS model inference : ${end - current}")
        return checkThreshold(outputFeature0.floatArray)
    }

    private fun checkThreshold(out1: FloatArray): Boolean {
        val realP = out1[0]
        val spoofP = out1[1]
        Log.w("AntiSpoofing", "Real probability: $realP")
        Log.w("AntiSpoofing", "Spoof probability: $spoofP")
        return (realP > 0.9 && spoofP < 0.1)
    }

    fun close() {
        model.close()
    }

    companion object {
        const val PROBABILITY_THRESHOLD = 0.5
        const val LAPLACE_THRESHOLD = 50
        const val LAPLACE_FINAL_THRESHOLD = 1000
        val MEAN = floatArrayOf(151.2405f, 119.5950f, 107.8395f)
        val SCALE = floatArrayOf(63.0105f, 56.4570f, 55.0035f)

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