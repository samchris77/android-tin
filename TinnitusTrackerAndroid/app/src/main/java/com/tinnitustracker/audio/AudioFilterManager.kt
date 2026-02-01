package com.tinnitustracker.audio

import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.DynamicsProcessing.Eq
import android.media.audiofx.DynamicsProcessing.EqBand
import android.util.Log

object AudioFilterManager {
    private const val TAG = "AudioFilterManager"
    private const val PRIORITY = Int.MAX_VALUE
    private const val SESSION_ID = 0 // Global Audio Session

    private var dynamicsProcessing: DynamicsProcessing? = null

    /**
     * Starts the audio filter targeting the specific frequency.
     * @param centerFrequencyHz The frequency to attenuate (e.g., 1000f)
     * @param attenuationDb The amount of attenuation in decibels (should be negative, e.g., -10f).
     *                      Positive values will boost the frequency.
     */
    fun startFilter(centerFrequencyHz: Float, attenuationDb: Float) {
        stopFilter() // Clear existing effect

        try {
            val builder = DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                1, // Number of channels (1 for global usually sufficient, or matching output)
                true, // Enable preEq
                1, // preEq bands
                true, // Enable mbc (needed for structure but can be neutral)
                1, // mbc bands
                true, // Enable postEq
                1, // postEq bands
                true // Enable limiter
            )

            val config = builder.build()
            
            // Configure the EQ to act as a Notch/Peaking filter
            // Using PostEQ for the final filtering
            val eqBand = EqBand(true, centerFrequencyHz, attenuationDb)
            
            // Set the configuration for the band
            // We use a relatively high Q-factor for a notch-like effect, or standard for bell
            // 1.0f is standard width. Higher values (e.g., 5.0f-10.0f) make it narrower (notch).
            // Let's us a Q of 5.0 for a precise cut functionality.
            // Note: DynamicsProcessing API doesn't expose Q directly on EqBand constructor easily 
            // in all Android versions or wrappers, but let's assume standard Peaking/Bell behavior via gain.
            // Actually EqBand object just holds values. We need to set them on the engine.
            
            dynamicsProcessing = DynamicsProcessing(SESSION_ID).apply {
                enabled = true
                
                // We use PreEq or PostEq. Let's use PreEq.
                val preEq = Eq(true, true, 1)
                preEq.getBand(0).apply {
                    enabled = true
                    cutoffFrequency = centerFrequencyHz
                    gain = attenuationDb
                    // There isn't a direct "Q" or "Bandwidth" setter on the standard EqBand object 
                    // dependent on API level details, usually it's just setCutoffFrequency and gain for basic shelving/peaking.
                    // For more complex notch, we might need multi-band, but a single band peaking EQ with negative gain acts as a cut.
                }
                setPreEqAllChannelsTo(preEq)
            }

            Log.d(TAG, "Audio filter started at ${centerFrequencyHz}Hz with ${attenuationDb}dB")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize DynamicsProcessing: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * stops the audio filter and releases resources
     */
    fun stopFilter() {
        dynamicsProcessing?.let {
            it.enabled = false
            it.release()
            dynamicsProcessing = null
            Log.d(TAG, "Audio filter stopped")
        }
    }
}
