package zone.vgs.tvsach.dulieu

import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AquaGalaxyTest {
    private val galaxy = THEMES.single { it.id == "ang-hon-aqua-galaxy" }
    private val legacy = THEMES.single { it.id == "ang-hon-aqua-lite" }

    @Test fun galaxyDefaultsMatchManagerAndCanBeImported() {
        val defaults = JSONObject(File("../themes/ang-hon-aqua-galaxy/config.json").readText())
        val config = JSONObject().also { galaxy.ghiVao(it) }
        galaxy.cauHinh.keys.forEach { assertEquals(it, defaults.get(it), config.get(it)) }
        assertEquals(galaxy.id, defaults.getString("vgs_theme"))
        assertEquals(1, defaults.getInt("vgs_file"))
        assertEquals("Ăng Hòn", defaults.getString("greeting_name"))
        assertTrue(galaxy.laAqua)
        assertTrue(legacy.laAqua)
    }

    @Test fun legacyPresetAndMotionSettingsArePreserved() {
        val defaults = JSONObject(File("../themes/ang-hon-aqua-lite/config.json").readText())
        legacy.cauHinh.forEach { (k,v) -> assertEquals(k, defaults.get(k), v) }
        assertFalse(legacy.cauHinh.containsKey("aqua_galaxy"))
        legacy.cauHinh.filterKeys { !it.endsWith("_color") }.forEach { (k,v) -> assertEquals(k,v,galaxy.cauHinh[k]) }
        assertTrue(themesChoBan(true).containsAll(listOf(legacy,galaxy)))
    }

    @Test fun oldColourOverridesDoNotReplaceGalaxyColourKeys() {
        val config=JSONObject().also { galaxy.ghiVao(it) }
        config.put("aqua_gradient_color","#FF0000").put("aqua_night_color","#FFFFFF")
        assertEquals("#07121D",config.getString("galaxy_space_color"))
        assertEquals("#1A6875",config.getString("galaxy_ocean_color"))
        assertEquals("#4256A8",config.getString("galaxy_indigo_color"))
    }

    @Test fun galaxyColoursRoundTripThroughAdvancedConfiguration() {
        val catalog=JSONArray(File("src/main/assets/nang-cao.json").readText())
        val config=JSONObject().also { galaxy.ghiVao(it) }
        for (key in galaxy.cauHinh.keys.filter { it.startsWith("galaxy_") }) {
            val entry=(0 until catalog.length()).map { catalog.getJSONObject(it) }.single { it.getString("khoa")==key }
            val option=TuyChon(key,entry.getString("kieu"),Int.MIN_VALUE,Int.MAX_VALUE,emptyList(),"",false)
            config.put(key,NangCao.giaTri(option," #204060 "))
            assertEquals("#204060",NangCao.docChu(option,JSONObject(config.toString())))
        }
    }
}
