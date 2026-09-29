package ch.simibu.pace

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Validates localization parity across all 20 supported European languages.
 * Ensures no string keys are missing and no values are blank.
 */
class LocalizationTest {

    private val resDir = File("src/main/res").takeIf { it.exists() } ?: File("app/src/main/res")

    private fun extractStringKeys(file: File): Map<String, String> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(file)
        doc.documentElement.normalize()

        val stringNodes = doc.getElementsByTagName("string")
        val map = mutableMapOf<String, String>()

        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            if (node is Element) {
                val name = node.getAttribute("name")
                val text = node.textContent.trim()
                map[name] = text
            }
        }
        return map
    }

    @Test
    fun testDefaultStringsExistAndNotEmpty() {
        val baseFile = File(resDir, "values/strings.xml")
        assertTrue("Base strings.xml must exist at ${baseFile.absolutePath}", baseFile.exists())

        val baseKeys = extractStringKeys(baseFile)
        assertTrue("Base strings must contain at least 20 keys", baseKeys.size >= 20)

        // Verify key application elements
        val criticalKeys = listOf(
            "app_name",
            "nav_quick_start",
            "nav_routines",
            "nav_settings",
            "timer_phase_focus",
            "timer_phase_break",
            "timer_phase_warmup",
            "timer_phase_cooldown",
            "timer_phase_complete",
            "start_timer",
            "btn_pause",
            "btn_resume",
            "btn_skip",
            "btn_stop"
        )

        for (key in criticalKeys) {
            assertTrue("Base strings.xml must contain critical key '$key'", baseKeys.containsKey(key))
            assertFalse("Value for '$key' cannot be empty", baseKeys[key].isNullOrBlank())
        }
    }

    @Test
    fun testAll20LanguagesHaveParityWithBaseStrings() {
        val baseFile = File(resDir, "values/strings.xml")
        val baseKeys = extractStringKeys(baseFile)

        val targetLanguages = listOf(
            "de", "fr", "es", "it", "pt", "nl", "pl", "uk", "ru", "tr",
            "sv", "cs", "el", "ro", "hu", "da", "fi", "nb", "hr"
        )

        val missingReport = mutableListOf<String>()

        for (lang in targetLanguages) {
            val langDir = File(resDir, "values-$lang")
            assertTrue("Resource directory values-$lang must exist", langDir.exists())

            val langFile = File(langDir, "strings.xml")
            assertTrue("strings.xml must exist in values-$lang", langFile.exists())

            val langKeys = extractStringKeys(langFile)

            for (key in baseKeys.keys) {
                if (!langKeys.containsKey(key)) {
                    missingReport.add("Language '$lang' is missing key: '$key'")
                } else if (langKeys[key].isNullOrBlank()) {
                    missingReport.add("Language '$lang' has empty translation for key: '$key'")
                }
            }
        }

        assertTrue(
            "Localization parity issues detected:\n" + missingReport.joinToString("\n"),
            missingReport.isEmpty()
        )
    }
}
