package com.example.cardgame.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.cardgame.R

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
        MusicTrack.DECK_EDITOR to R.raw.ravel_cut
        // You can add more tracks here
    )

    /**
     * Start playing a music track
     */
    fun playMusic(track: MusicTrack, loop: Boolean = true) {
        // Don't restart if already playing this track
        if (currentTrack == track && mediaPlayer?.isPlaying == true) {
            return
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

        } catch (e: Exception) {
            Log.e("MusicManager", "Error playing music: ${e.message}")
        }
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