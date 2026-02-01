package com.tinnitustracker.audio

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class BiquadFilter(
    private val sampleRate: Int
) {
    private var a0 = 1.0
    private var a1 = 0.0
    private var a2 = 0.0
    private var b0 = 1.0
    private var b1 = 0.0
    private var b2 = 0.0

    private var z1 = 0.0
    private var z2 = 0.0

    // BandPass filter implementation
    fun setBandPass(frequency: Float, q: Float) {
        val omega = 2.0 * PI * frequency / sampleRate
        val alpha = sin(omega) / (2.0 * q)

        b0 = alpha
        b1 = 0.0
        b2 = -alpha
        a0 = 1.0 + alpha
        a1 = -2.0 * cos(omega)
        a2 = 1.0 - alpha
        
        preNormalize()
    }

    // Notch Filter implementation
    fun setNotch(frequency: Float, q: Float) {
        val omega = 2.0 * PI * frequency / sampleRate
        val alpha = sin(omega) / (2.0 * q)

        b0 = 1.0
        b1 = -2.0 * cos(omega)
        b2 = 1.0
        a0 = 1.0 + alpha
        a1 = -2.0 * cos(omega)
        a2 = 1.0 - alpha

        preNormalize()
    }

    private fun preNormalize() {
        // Pre-normalize coefficients to save divisions during processing
        val invA0 = 1.0 / a0
        b0 *= invA0
        b1 *= invA0
        b2 *= invA0
        a1 *= invA0
        a2 *= invA0
    }

    fun reset() {
        z1 = 0.0
        z2 = 0.0
    }

    fun process(sample: Float): Float {
        // Direct Form II Transposed implementation
        val out = b0 * sample + z1
        z1 = b1 * sample + z2 - a1 * out
        z2 = b2 * sample - a2 * out
        return out.toFloat()
    }
}
