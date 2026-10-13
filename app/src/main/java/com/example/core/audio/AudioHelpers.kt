package com.example.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.log10

class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var samplingJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Int>>(emptyList())
    val amplitudes: StateFlow<List<Int>> = _amplitudes.asStateFlow()

    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    fun startRecording(outputFile: File): Boolean {
        return try {
            currentOutputFile = outputFile
            _amplitudes.value = emptyList()
            _elapsedMillis.value = 0L

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            _isRecording.value = true

            // Sample amplitudes every 100ms
            samplingJob = CoroutineScope(Dispatchers.Default).launch {
                val start = System.currentTimeMillis()
                val sampledList = mutableListOf<Int>()
                while (isActive && _isRecording.value) {
                    delay(100)
                    _elapsedMillis.value = System.currentTimeMillis() - start
                    val maxAmp = try {
                        recorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    // Normalize to 0..100
                    val normalized = if (maxAmp > 0) {
                        val db = 20 * log10(maxAmp.toDouble())
                        ((db / 90.0) * 100.0).toInt().coerceIn(8, 100)
                    } else 8

                    sampledList.add(normalized)
                    _amplitudes.value = sampledList.takeLast(64)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
            false
        }
    }

    fun stopRecording(): Pair<File?, List<Int>> {
        samplingJob?.cancel()
        samplingJob = null
        val finalSamples = _amplitudes.value.ifEmpty { List(64) { 15 } }

        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
        _isRecording.value = false

        // Normalize samples to exactly 64 bars
        val resampled = resampleTo64Bars(finalSamples)
        return Pair(currentOutputFile, resampled)
    }

    private fun resampleTo64Bars(input: List<Int>): List<Int> {
        if (input.isEmpty()) return List(64) { 20 }
        if (input.size == 64) return input
        val result = mutableListOf<Int>()
        for (i in 0 until 64) {
            val idx = ((i.toDouble() / 63.0) * (input.size - 1)).toInt().coerceIn(0, input.size - 1)
            result.add(input[idx])
        }
        return result
    }
}

class AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _speed = MutableStateFlow(1.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    fun play(filePath: String, onCompletion: (() -> Unit)? = null) {
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    playbackParams = playbackParams.setSpeed(_speed.value)
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    progressJob?.cancel()
                    _currentPosition.value = 0
                    onCompletion?.invoke()
                }
                start()
            }
            _duration.value = mediaPlayer?.duration ?: 0
            _isPlaying.value = true

            startProgressTracker()
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                progressJob?.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _currentPosition.value = positionMs
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSpeed(newSpeed: Float) {
        _speed.value = newSpeed
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
                mediaPlayer?.playbackParams = mediaPlayer!!.playbackParams.setSpeed(newSpeed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPosition.value = 0
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && _isPlaying.value) {
                val pos = mediaPlayer?.currentPosition ?: 0
                _currentPosition.value = pos
                delay(100)
            }
        }
    }
}
