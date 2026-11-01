package zone.vgs.tvsach.dulieu

import java.io.File
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AquaCosmicPlusTest {
    private val plus = THEMES.single { it.id == "ang-hon-aqua-cosmic-plus" }
    private val cosmic = THEMES.single { it.id == "ang-hon-aqua-cosmic" }

    @Test fun freshDefaultsMatchPlusPresetAndInheritCinematicMotion() {
        val defaults = JSONObject(File("../themes/ang-hon-aqua-cosmic-plus/config.json").readText())
        val config = JSONObject().also { plus.ghiVao(it) }
        plus.cauHinh.forEach { (key, value) -> assertEquals(key, value, defaults.get(key)); assertEquals(key, value, config.get(key)) }
        cosmic.cauHinh.forEach { (key, value) -> assertEquals(key, value, plus.cauHinh[key]) }
        assertEquals(plus.id, defaults.getString("vgs_theme"))
        assertTrue(plus.laAqua); assertTrue(config.getBoolean("aqua_cosmic_plus"))
        assertEquals("Ăng Hòn", defaults.getString("greeting_name"))
    }

    @Test fun plusRoundTripAndRollbackKeepPersonalSettings() {
        val config = JSONObject("""{"greeting_name":"Gia đình Hòn Đảo","dock_apps":["org.example.tv"],"app_keys":["190:org.example.tv"]}""")
        val personal = JSONObject(config.toString())
        plus.ghiVao(config)
        val restored = JSONObject(config.toString())
        assertEquals(plus.id, restored.getString("vgs_theme"))
        assertTrue(restored.getBoolean("aqua_cosmic_plus"))
        cosmic.ghiVao(restored)
        assertFalse(restored.optBoolean("aqua_cosmic_plus", false))
        assertEquals(cosmic.id, restored.getString("vgs_theme"))
        for (key in listOf("greeting_name", "dock_apps", "app_keys")) assertEquals(personal.get(key).toString(), restored.get(key).toString())
    }

    @Test fun legacyPresetDefaultsStaySelectableWithoutPlus() {
        for (id in listOf("ang-hon-aqua-lite", "ang-hon-aqua-galaxy", "ang-hon-aqua-cosmic")) {
            val legacy = themesChoBan(true).single { it.id == id }
            val defaults = JSONObject(File("../themes/$id/config.json").readText())
            val config = JSONObject().also { legacy.ghiVao(it) }
            legacy.cauHinh.forEach { (key, value) -> assertEquals(key, value, defaults.get(key)); assertEquals(key, value, config.get(key)) }
            assertFalse(config.optBoolean("aqua_cosmic_plus", false))
            assertFalse(defaults.optBoolean("aqua_cosmic_plus", false))
        }
    }
}
