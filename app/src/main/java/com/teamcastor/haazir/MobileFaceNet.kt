/*
package com.teamcastor.haazir

import android.content.Context
import android.graphics.Bitmap
import com.teamcastor.haazir.ml.MobileFaceNet
import org.tensorflow.lite.*
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class MobileFaceNet(context: Context, img1: Bitmap, img2: Bitmap) {
    val model = MobileFaceNet.newInstance(context)
    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(2, 112, 112, 3), DataType.FLOAT32)
    var byteBuffer = copyImagesToBuffer(img1, img2)
    val outputs = model.process(inputFeature0)
    val outputFeature0 = outputs.outputFeature0AsTensorBuffer


    companion object {
        fun copyImagesToBuffer(img1: Bitmap, img2: Bitmap): ByteBuffer {
            var byteBuffer = ByteBuffer.allocate(112 * 112 * 2 * 3 * 4)
            img1.copyPixelsToBuffer(byteBuffer)
            img2.copyPixelsToBuffer(byteBuffer)
            return byteBuffer
        }
    }
}*/
