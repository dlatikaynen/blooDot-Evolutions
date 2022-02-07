package oy.sarjakuvat.flamingin.bde

import oy.sarjakuvat.flamingin.bde.gles.GpuSourceLoader.Companion.loadGpuSourceCode
import android.app.ListActivity
import android.os.Bundle
import android.widget.SimpleAdapter
import android.content.Intent
import android.view.View
import android.widget.ListView
import java.lang.RuntimeException
import java.util.ArrayList
import java.util.HashMap

class MainActivity : ListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listAdapter = SimpleAdapter(
            this,
            buildMainMenu(),
            android.R.layout.two_line_list_item,
            arrayOf(TITLE, DESCRIPTION),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
    }

    private fun buildMainMenu(): List<Map<String, Any?>> {
        val menuItems: MutableList<Map<String, Any?>> = ArrayList()
        for (menuItem in mainMenuItems) {
            val tmp: MutableMap<String, Any?> = HashMap()
            tmp[TITLE] = menuItem[0]
            tmp[DESCRIPTION] = menuItem[1]
            try {
                val module = Class.forName("oy.sarjakuvat.flamingin.bde.${menuItem[2]}")
                val intent = Intent()
                intent.setClass(this, module)
                tmp[CLASS_NAME] = intent
            } catch (cEx: ClassNotFoundException) {
                throw RuntimeException("Menu corrupted, missing ${menuItem[2]}", cEx)
            }

            menuItems.add(tmp)
        }

        return menuItems
    }

    override fun onListItemClick(listView: ListView, view: View, position: Int, id: Long) {
        val map = listView.getItemAtPosition(position) as Map<*, *>
        val intent = map[CLASS_NAME] as Intent?
        startActivity(intent)
    }

    companion object {
        const val TAG = "bloo_dot_evolution"
        private const val TITLE = "title"
        private const val DESCRIPTION = "description"
        private const val CLASS_NAME = "class_name"

        private val mainMenuItems = arrayOf(
            arrayOf(
                "The blooDot Evolutions Game",
                "Development Environment",
                "GameActivity"
            )
        )

        @JvmStatic
        val gpuSourceFlatVertexShader: String get() = loadGpuSourceCode(R.raw.flat_vertex_shader)

        @JvmStatic
        val gpuSourceFlatFragmentShader: String get() = loadGpuSourceCode(R.raw.flat_fragment_shader)

        @JvmStatic
        val gpuSourceTextureFragmentShader2d: String get() = loadGpuSourceCode(R.raw.texture_fragment_shader_2d)

        @JvmStatic
        val gpuSourceTextureFragmentShader2dBw: String get() = loadGpuSourceCode(R.raw.texture_fragment_shader_2d_bw)

        @JvmStatic
        val gpuSourceTextureFragmentShaderConvolutions: String get() = loadGpuSourceCode(R.raw.texture_fragment_shader_convolution)

        @JvmStatic
        val gpuSourceTextureFragmentShaderExternal: String get() = loadGpuSourceCode(R.raw.texture_fragment_shader_external)

        @JvmStatic
        val gpuSourceTextureFragmentShaderExternalBw: String get() = loadGpuSourceCode(R.raw.texture_fragment_shader_external_bw)

        @JvmStatic
        val gpuSourceTextureVertexShader: String get() = loadGpuSourceCode(R.raw.texture_vertex_shader)
    }
}
