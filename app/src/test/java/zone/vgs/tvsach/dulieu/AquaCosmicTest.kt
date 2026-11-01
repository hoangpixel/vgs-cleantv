package zone.vgs.tvsach.dulieu

import java.io.File
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AquaCosmicTest {
    private val cosmic=THEMES.single { it.id=="ang-hon-aqua-cosmic" }
    private val galaxy=THEMES.single { it.id=="ang-hon-aqua-galaxy" }
    @Test fun defaultsMatchManagerAndStackedHeaderIsEnabled() {
        val defaults=JSONObject(File("../themes/ang-hon-aqua-cosmic/config.json").readText())
        val config=JSONObject().also { cosmic.ghiVao(it) }
        cosmic.cauHinh.forEach { (k,v)->assertEquals(k,v,defaults.get(k));assertEquals(k,v,config.get(k)) }
        assertEquals(cosmic.id,defaults.getString("vgs_theme"));assertTrue(cosmic.laAqua)
        assertTrue(config.getBoolean("aqua_cosmic"));assertTrue(config.getBoolean("date_below_clock"))
    }
    @Test fun galaxyPaletteAndMotionAreInheritedWithoutChangingOldPreset() {
        galaxy.cauHinh.forEach { (k,v)->assertEquals(k,v,cosmic.cauHinh[k]) }
        assertFalse(galaxy.cauHinh.containsKey("aqua_cosmic"))
        val old=JSONObject(File("../themes/ang-hon-aqua-galaxy/config.json").readText())
        galaxy.cauHinh.forEach { (k,v)->assertEquals(k,v,old.get(k)) }
        assertTrue(themesChoBan(true).any { it.id=="ang-hon-aqua-lite" })
    }
    @Test fun nameDockAndRemoteSurviveCosmicPresetAndRoundTrip() {
        val config=JSONObject("""{"greeting_name":"Gia đình Hòn Đảo","dock_apps":["org.example.tv"],"app_keys":["190:org.example.tv"]}""")
        val before=config.toString();cosmic.ghiVao(config)
        val roundTrip=JSONObject(config.toString());val original=JSONObject(before)
        for(k in listOf("greeting_name","dock_apps","app_keys"))assertEquals(original.get(k).toString(),roundTrip.get(k).toString())
        assertEquals(cosmic.id,roundTrip.getString("vgs_theme"))
    }
}
