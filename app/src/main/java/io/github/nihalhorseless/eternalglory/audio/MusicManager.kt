package io.github.nihalhorseless.eternalglory.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.nihalhorseless.eternalglory.R

class MusicManager(private val context: Context) {



    // MediaPlayer instance for background music
    private var mediaPlayer: MediaPlayer? = null

    // Track what's currently playing
    private var currentTrack: MusicTrack? = null

    // Volume level (0.0f to 1.0f)
    private var volume: Float = 0.5f

    // Track resource mapping
    private val musicResources = mapOf(
        MusicTrack.MAIN_MENU to R.raw.purcel_cut,
        MusicTrack.LEVEL_SELECTION to R.raw.handel_cut,
        MusicTrack.DECK_EDITOR to R.raw.strauss_guitar
        // You can add more tracks here
    )

    // Mute state
    private val _isMuted = mutableStateOf(false)
    val isMuted: State<Boolean> = _isMuted

    /**
     * Start playing a music track
     */
    fun playMusic(track: MusicTrack, loop: Boolean = true) {
        // Don't restart if already playing this track or it's muted
        if (currentTrack == track && mediaPlayer?.isPlaying == true || isMuted.value) {
            return
        }

        volume = when (track) {
            MusicTrack.MAIN_MENU -> 0.1f
            MusicTrack.DECK_EDITOR -> 0.6f
            else -> 0.3f
        }

        // Stop any current playback
        stopMusic()

        try {
            // Get the resource ID for this track
            val resourceId = musicResources[track] ?: return

            // Create and configure a new MediaPlayer
            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.apply {
                isLooping = loop
                setVolume(volume, volume)
                start()
            }

            currentTrack = track

        } catch (_: Exception) {

        }
    }
    /**
     * Toggle mute status
     */
    fun toggleMute() {
        _isMuted.value = !_isMuted.value

        // Apply the new mute state to the current player
        mediaPlayer?.setVolume(
            if (_isMuted.value) 0f else volume,
            if (_isMuted.value) 0f else volume
        )
    }
    /**
     * Check if music is currently playing
     */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
    /**
     * Stop the currently playing music
     */
    fun stopMusic() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentTrack = null
    }

    /**
     * Pause the currently playing music
     */
    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    /**
     * Resume paused music
     */
    fun resumeMusic() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    /**
     * Set the music volume
     * @param volume Value between 0.0 (silent) and 1.0 (full volume)
     */
    fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(this.volume, this.volume)
    }

    /**
     * Release resources when no longer needed
     */
    fun release() {
        stopMusic()
    }
}