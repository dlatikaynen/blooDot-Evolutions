package oy.sarjakuvat.flamingin.bde.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import oy.sarjakuvat.flamingin.bde.R

object SoundOrchestrator {
    private const val MaxParallelStreams = 5

    var soundEffectOpening = 0

    private var soundPool: SoundPool? = null

    fun initializeSoundOrchestrator(context: Context) {
        if(soundPool == null) {
            val audioAttributes =AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(MaxParallelStreams)
                .setAudioAttributes(audioAttributes)
                .build()
        }

        soundEffectOpening = soundPool?.load(context, R.raw.opening_sample,1) ?: soundEffectOpening
    }

    fun playSoundeffect(effectId: Int) {
        if(effectId > 0) {
            soundPool?.play(effectId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun shutDown() {
        soundPool?.release()
    }
}
