package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.InspectionReport
import com.example.data.model.Restaurant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceSynthesisManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeSpeakingId = MutableStateFlow<String?>(null)
    val activeSpeakingId: StateFlow<String?> = _activeSpeakingId.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _activeSpeakingId.value = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _activeSpeakingId.value = null
                    }
                })
            }
        }
    }

    fun speakListing(restaurant: Restaurant, reports: List<InspectionReport> = emptyList()) {
        if (!isInitialized) return

        // If currently speaking this listing, stop it
        if (_isSpeaking.value && _activeSpeakingId.value == restaurant.id) {
            stop()
            return
        }

        stop()

        val textToSpeak = buildString {
            append("Reading CleanBite report for ${restaurant.name}. ")
            append("Cuisine type ${restaurant.cuisine}, price range ${restaurant.priceRange}, located at ${restaurant.address} in ${restaurant.neighborhood}. ")
            append("Official health grade is Grade ${restaurant.healthGrade} with an inspection score of ${restaurant.healthScore} out of 100. ")
            if (restaurant.criticalViolationsCount > 0) {
                append("Warning: ${restaurant.criticalViolationsCount} critical health violations recorded. ")
            } else {
                append("Pristine record with zero critical violations. ")
            }
            append("Consumer rating is ${restaurant.consumerRating} stars out of 5 based on ${restaurant.reviewCount} customer reviews. ")

            if (reports.isNotEmpty()) {
                val latest = reports.first()
                append("Latest inspection on ${latest.date}: Inspector reports ${latest.inspectorNotes}. ")
            }
        }

        _activeSpeakingId.value = restaurant.id
        _isSpeaking.value = true
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, restaurant.id)
    }

    fun speakText(id: String, text: String) {
        if (!isInitialized) return

        if (_isSpeaking.value && _activeSpeakingId.value == id) {
            stop()
            return
        }

        stop()
        _activeSpeakingId.value = id
        _isSpeaking.value = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _activeSpeakingId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
