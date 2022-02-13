package oy.sarjakuvat.flamingin.bde

import android.app.Activity
import android.content.pm.ActivityInfo
import android.icu.util.ULocale
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import oy.sarjakuvat.flamingin.bde.level.Arena
import oy.sarjakuvat.flamingin.bde.rendition.RenderThread


class GameActivity : Activity(), SurfaceHolder.Callback, Choreographer.FrameCallback {
    private var selectedSpriteGrade = 0
    private var fullScreenWidth = 0
    private var fullScreenHeight = 0
    private var useFlatShading = false
    private var enableTestScrolling = false
    private var renderThread: RenderThread? = null

    private lateinit var screenDimensions: Array<IntArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "GameActivity::onCreate")
        requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val metrics: DisplayMetrics = App.context.get()!!.resources.displayMetrics
        fullScreenHeight = metrics.widthPixels / 4
        fullScreenWidth = metrics.heightPixels / 4
        screenDimensions = Array(SURFACE_DIM.size) { IntArray(2) }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        selectedSpriteGrade = SURFACE_SIZE_FULL
        updateControls()
        val surfaceView = findViewById<SurfaceView>(R.id.game_surfaceView)
        surfaceView.holder.setFixedSize(fullScreenHeight, fullScreenWidth)
        surfaceView.holder.addCallback(this)
    }

    private fun prepareArena() {
        Arena.load()
        if(BuildConfig.DEBUG) {
            GlobalScope.launch { Arena.dumpDebugInfo() }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause pulling flinger brake")
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun onResume() {
        super.onResume()
        if (renderThread != null) {
            Log.d(TAG, "onResume releasing flinger brake")
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated called with $holder as its holder")
        val size = holder.surfaceFrame
        fullScreenWidth = size.width()
        fullScreenHeight = size.height()
        val windowAspect = fullScreenHeight.toFloat() / fullScreenWidth.toFloat()
        for (i in SURFACE_DIM.indices) {
            when {
                i == SURFACE_SIZE_FULL -> {
                    screenDimensions[i][0] = fullScreenWidth
                    screenDimensions[i][1] = fullScreenHeight
                }
                fullScreenWidth < fullScreenHeight -> {
                    /* portrait orientation */
                    screenDimensions[i][0] = SURFACE_DIM[i]
                    screenDimensions[i][1] = (SURFACE_DIM[i] * windowAspect).toInt()
                }
                else -> {
                    /* landscape orientation */
                    screenDimensions[i][0] = (SURFACE_DIM[i] / windowAspect).toInt()
                    screenDimensions[i][1] = SURFACE_DIM[i]
                }
            }
        }

        updateControls()
        val surfaceView = findViewById<SurfaceView>(R.id.game_surfaceView)
        renderThread = RenderThread(surfaceView.holder)
        renderThread!!.name = "blooDot Evolutions OpenGL ES renderer"
        renderThread!!.start()
        renderThread!!.waitUntilReady()

        val renderHandler = renderThread!!.handler
        renderHandler?.sendSetFlatShading(useFlatShading)
        renderHandler?.sendTestScrolling(enableTestScrolling)
        renderHandler?.sendSurfaceCreated()

        /* load our working data */
        prepareArena()

        /* kick it off */
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged fmt=$format size=$width by $height; holder=$holder")
        val rh = renderThread!!.handler
        rh?.sendSurfaceChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed with $holder as its holder")
        val rh = renderThread!!.handler
        if (rh != null) {
            rh.sendShutdown()
            try {
                renderThread!!.join()
            } catch (ie: InterruptedException) {
                throw RuntimeException("Interrupted whilst attempting to join the render thread", ie)
            }
        }

        renderThread = null
        Log.d(TAG, "surfaceDestroyed done")
    }

    /// Syncs to the display update frequency
    override fun doFrame(frameTimeNanos: Long) {
        val rh = renderThread!!.handler
        if (rh != null) {
            Choreographer.getInstance().postFrameCallback(this)
            rh.sendDoFrame(frameTimeNanos)
        }
    }

    fun onRadioButtonClicked(view: View) {
        val newSize: Int
        val radioButton = view as RadioButton
        if (!radioButton.isChecked) {
            return
        }

        val resId = radioButton.id
        newSize = when (resId) {
            R.id.surfaceSizeTiny_radio -> {
                SURFACE_SIZE_TINY
            }
            R.id.surfaceSizeSmall_radio -> {
                SURFACE_SIZE_SMALL
            }
            R.id.surfaceSizeMedium_radio -> {
                SURFACE_SIZE_MEDIUM
            }
            R.id.surfaceSizeFull_radio -> {
                SURFACE_SIZE_FULL
            }
            else -> {
                throw RuntimeException("Invalid game control input ${radioButton.id}")
            }
        }

        selectedSpriteGrade = newSize
        val wh = screenDimensions[newSize]
        val sv = findViewById<SurfaceView>(R.id.game_surfaceView)
        val sh = sv.holder
        Log.d(TAG, "setting size to " + wh[0] + "x" + wh[1])
        sh.setFixedSize(wh[0], wh[1])
    }

    @Suppress("UNUSED_PARAMETER")
    fun onFlatShadingClicked(view: View) {
        val checkBox = findViewById<CheckBox>(R.id.flatShading_checkbox)
        useFlatShading = checkBox.isChecked
        renderThread!!.handler?.sendSetFlatShading(useFlatShading)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onTestScrollingClicked(view: View) {
        val checkBox = findViewById<CheckBox>(R.id.testScrolling_checkbox)
        enableTestScrolling = checkBox.isChecked
        renderThread!!.handler?.sendTestScrolling(enableTestScrolling)
    }

    private fun updateControls() {
        configureRadioButton(R.id.surfaceSizeTiny_radio, SURFACE_SIZE_TINY)
        configureRadioButton(R.id.surfaceSizeSmall_radio, SURFACE_SIZE_SMALL)
        configureRadioButton(R.id.surfaceSizeMedium_radio, SURFACE_SIZE_MEDIUM)
        configureRadioButton(R.id.surfaceSizeFull_radio, SURFACE_SIZE_FULL)

        val viewSize = findViewById<TextView>(R.id.viewSizeValue_text)
        viewSize.text = String.format("%dx%d", fullScreenWidth, fullScreenHeight)

        val checkBox = findViewById<CheckBox>(R.id.flatShading_checkbox)
        checkBox.isChecked = useFlatShading
    }

    private fun configureRadioButton(id: Int, index: Int) {
        val radioButton: RadioButton = findViewById(id)
        radioButton.isChecked = selectedSpriteGrade == index
        radioButton.text = String.format(
            ULocale.getDefault().toLocale(), "%s (%dx%d)",
            SURFACE_LABEL[index],
            screenDimensions[index][0],
            screenDimensions[index][1]
        )
    }

    companion object {
        const val TAG = MainActivity.TAG

        private const val SURFACE_SIZE_TINY = 0
        private const val SURFACE_SIZE_SMALL = 1
        private const val SURFACE_SIZE_MEDIUM = 2
        private const val SURFACE_SIZE_FULL = 3

        private val SURFACE_DIM = intArrayOf(64, 240, 480, -1)
        private val SURFACE_LABEL = arrayOf(
            "fucked-up",
            "small",
            "medium",
            "device"
        )
    }
}
