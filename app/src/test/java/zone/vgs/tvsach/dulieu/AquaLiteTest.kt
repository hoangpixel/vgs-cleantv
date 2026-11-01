package zone.vgs.tvsach.dulieu

import org.json.JSONObject
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AquaLiteTest {
    private val theme = THEMES.single { it.id == "ang-hon-aqua-lite" }

    @Test fun phonePresetMatchesFreshLauncherDefaults() {
        val defaults = JSONObject(File("../themes/ang-hon-aqua-lite/config.json").readText())
        val config = JSONObject().also { theme.ghiVao(it) }
        theme.cauHinh.keys.forEach { key -> assertEquals(key, defaults.get(key), config.get(key)) }
        assertEquals("Ăng Hòn", defaults.getString("greeting_name"))
        assertTrue(defaults.getBoolean("lunar_date"))
        assertFalse(config.getBoolean("wallpaper"))
    }

    @Test fun themePreservesDockAndRemoteConfiguration() {
        val config = JSONObject("""{"dock_apps":["org.example.video"],"app_keys":["190:org.example.video"],"key_filter":false,"greeting_name":"Gia đình"}""")
        val before = JSONObject(config.toString())
        theme.ghiVao(config)
        for (key in listOf("dock_apps", "app_keys", "key_filter", "greeting_name")) {
            assertEquals(key, before.get(key).toString(), config.get(key).toString())
        }
        assertFalse(config.getBoolean("reduced_motion"))
        for (key in listOf("animations", "cinematic_home", "ambient_glow", "focus_overshoot", "cinematic_dots", "aqua_cinematic")) {
            assertTrue(key, config.getBoolean(key))
        }
        for (key in listOf("advanced_effects", "dock_glass", "fade_rows", "uniform_tiles")) {
            assertFalse(key, config.getBoolean(key))
        }
    }

    @Test fun cinematicPresetKeepsOriginalShelfAndMotionOptions() {
        val original = THEMES.single { it.id == "dien-anh" }
        original.cauHinh.forEach { (key, value) -> assertEquals(key, value, theme.cauHinh[key]) }
        assertEquals(50, theme.cauHinh["cinematic_art"])
        assertEquals(100, theme.cauHinh["cinematic_scrim"])
        assertEquals("glass", theme.cauHinh["glass_style"])
        assertEquals(false, theme.cauHinh["cinematic_white_focus"])
    }

    @Test fun visibleAquaPaletteKeepsArtworkAndScrimEnabled() {
        assertEquals("#126A76", theme.cauHinh["aqua_gradient_color"])
        assertEquals("#091923", theme.cauHinh["aqua_night_color"])
        assertEquals("#80DEDA", theme.cauHinh["focus_accent_color"])
        assertEquals(50, theme.cauHinh["cinematic_art"])
        assertEquals(100, theme.cauHinh["cinematic_scrim"])
        assertTrue(theme.tenVi.contains("v2.1"))
    }

    @Test fun customColoursAndNameSurviveConfigurationRoundTrip() {
        val config = JSONObject().also { theme.ghiVao(it) }
        val catalog = JSONArray(File("src/main/assets/nang-cao.json").readText())
        for ((key, value) in mapOf("aqua_gradient_color" to "#246B75", "aqua_night_color" to "#081320", "focus_accent_color" to "#90E5DE")) {
            val entry = (0 until catalog.length()).map { catalog.getJSONObject(it) }.single { it.getString("khoa") == key }
            val option = TuyChon(key, entry.getString("kieu"), Int.MIN_VALUE, Int.MAX_VALUE, emptyList(), "", false)
            config.put(key, NangCao.giaTri(option, "  $value  "))
            assertEquals(value, NangCao.docChu(option, JSONObject(config.toString())))
        }
        config.put("greeting_name", "Gia đình Hòn Đảo")
        val restored = JSONObject(config.toString())
        assertEquals("#246B75", restored.getString("aqua_gradient_color"))
        assertEquals("#081320", restored.getString("aqua_night_color"))
        assertEquals("Gia đình Hòn Đảo", restored.getString("greeting_name"))
        assertTrue(restored.getBoolean("animations"))
    }

    @Test fun personalThemePickerDoesNotOfferBrandedWallpapers() {
        assertTrue(themesChoBan(true).none { it.id == "vgs" || it.id == "macos" })
        assertEquals(THEMES, themesChoBan(false))
    }

    @Test fun defaultsCanBeImportedToRestoreTheAquaLook() {
        val defaults = JSONObject(File("../themes/ang-hon-aqua-lite/config.json").readText())
        assertEquals(1, defaults.getInt("vgs_file"))
        assertEquals(theme.id, defaults.getString("vgs_theme"))
        assertEquals("vi", defaults.getString("locale"))
    }
}
