package com.example.cardgame.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.cardgame.R

class SoundManager(private val context: Context) {

    private lateinit var soundPool: SoundPool
    private val soundMap = HashMap<SoundType, Int>()
    private var loaded = false


    fun initialize() {
        // Create audio attributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Create sound pool
        soundPool = SoundPool.Builder()
            .setMaxStreams(10) // Maximum number of simultaneous sounds
            .setAudioAttributes(audioAttributes)
            .build()

        // Set a listener to know when sounds are loaded
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) { // 0 means success
                loaded = true
            }
        }

        // Load all sound effects
        loadSounds()
    }

    private fun loadSounds() {
        try {
            // Attack sounds
            soundMap[SoundType.INFANTRY_ATTACK] = soundPool.load(context, R.raw.sword_slash_sound, 1)
            soundMap[SoundType.CAVALRY_ATTACK] = soundPool.load(context, R.raw.sword_slash_sound, 1)
            soundMap[SoundType.ARTILLERY_ATTACK] = soundPool.load(context, R.raw.artillery_unit_attack, 1)
            soundMap[SoundType.MISSILE_ATTACK] = soundPool.load(context, R.raw.missile_sound_three, 1)
            soundMap[SoundType.MUSKET_ATTACK] = soundPool.load(context, R.raw.muskeeter_sound, 1)

            // Card-related sounds
            soundMap[SoundType.CARD_PICK] = soundPool.load(context, R.raw.card_pick, 1)
            soundMap[SoundType.CARD_PLAY] = soundPool.load(context, R.raw.card_deploy, 1)

            // Spell effects
            soundMap[SoundType.SPELL_DEBUFF] = soundPool.load(context, R.raw.debuff, 1)
            soundMap[SoundType.SPELL_AREA_EFFECT] = soundPool.load(context, R.raw.tactic_card_explosion, 1)
            soundMap[SoundType.SPELL_BUFF] = soundPool.load(context, R.raw.fantasy_magic_button_1, 1)
            soundMap[SoundType.SPELL_SPECIAL] = soundPool.load(context, R.raw.fantasy_magic_button_1, 1)
            soundMap[SoundType.SPELL_DIRECT_DAMAGE] = soundPool.load(context, R.raw.rocket_effect, 1)

            // Unit and fortification sounds
            soundMap[SoundType.FOOT_UNIT_TAP] = soundPool.load(context, R.raw.foot_unit_tap, 1)
            soundMap[SoundType.FOOT_UNIT_MOVE] = soundPool.load(context, R.raw.foot_unit_move, 1)
            soundMap[SoundType.CAVALRY_UNIT_TAP] = soundPool.load(context, R.raw.cavalry_unit_tap, 1)
            soundMap[SoundType.CAVALRY_UNIT_MOVE] = soundPool.load(context, R.raw.cavalry_unit_move, 1)
            soundMap[SoundType.FORTIFICATION_TAP] = soundPool.load(context, R.raw.fortification_tap, 1)
            soundMap[SoundType.FORTIFICATION_DESTROY] = soundPool.load(context, R.raw.fortification_fall, 1)
            soundMap[SoundType.PLAYER_HIT] = soundPool.load(context, R.raw.attack_on_player, 1)
            soundMap[SoundType.DAMAGE_TAP] = soundPool.load(context, R.raw.damage_tick, 1)
            soundMap[SoundType.BAYONET_SHEATHE] = soundPool.load(context, R.raw.bayonet_sheathe, 1)


            // Game state sounds
            soundMap[SoundType.TURN_END] = soundPool.load(context, R.raw.end_turn_sound, 1)
            soundMap[SoundType.DEFEAT] = soundPool.load(context, R.raw.game_over, 1)
            soundMap[SoundType.VICTORY] = soundPool.load(context, R.raw.victory_chant, 1)
            soundMap[SoundType.MENU_TAP] = soundPool.load(context, R.raw.menu_click_one, 1)
            soundMap[SoundType.MENU_TAP_TWO] = soundPool.load(context, R.raw.menu_click_two, 1)
            soundMap[SoundType.MENU_SCROLL] = soundPool.load(context, R.raw.menu_page_turn, 1)
            soundMap[SoundType.LEVEL_START] = soundPool.load(context,R.raw.level_start,1)

        } catch (e: Exception) {
            Log.e("SoundManager", "Error loading sounds: ${e.message}")
        }
    }

    fun playSound(soundType: SoundType, volume: Float = 1.0f) {
        if (loaded && soundMap.containsKey(soundType)) {
            val soundId = soundMap[soundType] ?: return
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun release() {
        if (::soundPool.isInitialized) {
            soundPool.release()
        }
    }
}