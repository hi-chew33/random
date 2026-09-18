package com.vocis.webrtc.media

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.AudioTrackSink
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule

enum class VoipCallState {
    IDLE,
    CONNECTING,
    RINGING,
    CONNECTED,
    DISCONNECTED,
    FAILED
}

/**
 * WebRTC VoIP Media Engine with raw PCM audio tapping hooks.
 * Directs remote Opus-decoded 16kHz PCM frames to Phase 16 (VCD) and Phase 18 (Vosk ASR).
 */
class VoipMediaEngine(private val context: Context) {

    private val _callState = MutableStateFlow(VoipCallState.IDLE)
    val callState: StateFlow<VoipCallState> = _callState.asStateFlow()

    // Raw 16kHz PCM audio stream tapped from remote caller
    private val _remotePcmStream = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val remotePcmStream: SharedFlow<ByteArray> = _remotePcmStream.asSharedFlow()

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null

    val defaultIceServers: List<PeerConnection.IceServer> = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
    )

    /**
     * Initializes the WebRTC factory with hardware AEC and Noise Suppression.
     */
    fun initialize() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(false)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val audioDeviceModule = JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .createAudioDeviceModule()

        val rootEglBase = EglBase.create()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
            .createPeerConnectionFactory()

        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        }

        localAudioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("VOCIS_AUDIO_LOCAL", localAudioSource)
    }

    /**
     * Attaches an AudioTrackSink to tap incoming remote PCM frames.
     */
    fun attachRemoteAudioSink(remoteAudioTrack: AudioTrack) {
        val audioSink = AudioTrackSink { data, _, _, _, _, _ ->
            if (data != null && data.remaining() > 0) {
                val bytes = ByteArray(data.remaining())
                val slice = data.duplicate()
                slice.get(bytes)
                _remotePcmStream.tryEmit(bytes)
            }
        }
        remoteAudioTrack.addSink(audioSink)
    }

    fun setCallState(state: VoipCallState) {
        _callState.value = state
    }

    fun close() {
        try {
            peerConnection?.close()
            peerConnection = null
            localAudioTrack?.dispose()
            localAudioSource?.dispose()
            peerConnectionFactory?.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _callState.value = VoipCallState.IDLE
    }
}
