package oy.sarjakuvat.flamingin.bde

import android.app.Application
import android.content.Context
import oy.sarjakuvat.flamingin.bde.audio.SoundOrchestrator
import java.lang.ref.WeakReference

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = WeakReference<Context>( this)
        SoundOrchestrator.initializeSoundOrchestrator(context.get()!!)
    }

    override fun onTerminate() {
        SoundOrchestrator.shutDown()
        context.clear()
        super.onTerminate()
    }

    companion object {
        lateinit var context: WeakReference<Context>
            private set
    }
}
