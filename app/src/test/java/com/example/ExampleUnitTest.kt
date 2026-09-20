package com.example

import androidx.compose.ui.graphics.toArgb
import com.example.parser.SubtitleParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testSrtParsing() {
        val srtContent = """
            1
            00:01:20,000 --> 00:01:24,500
            Merhaba dünya!
            İkinci satır.

            2
            00:02:10,500 --> 00:02:15,000
            <i>İtalik metin</i>
        """.trimIndent()

        val cues = SubtitleParser.parseSrt(srtContent)
        assertEquals(2, cues.size)

        assertEquals(1, cues[0].id)
        assertEquals(80000L, cues[0].startTimeMs) // 1m 20s
        assertEquals(84500L, cues[0].endTimeMs)
        assertEquals("Merhaba dünya!\nİkinci satır.", cues[0].cleanText)

        assertEquals(2, cues[1].id)
        assertEquals(130500L, cues[1].startTimeMs) // 2m 10.5s
        assertEquals("İtalik metin", cues[1].cleanText)
    }

    @Test
    fun testAssParsing() {
        val assContent = SubtitleParser.getSampleAssContent()
        val cues = SubtitleParser.parseAss(assContent)

        assertTrue("Should parse at least 5 cues", cues.size >= 5)
        assertEquals("remsubs playground\nVideo ve Altyazı Önizleme Sistemi 💙", cues[0].cleanText)
        assertEquals(1000L, cues[0].startTimeMs)
        assertEquals(4500L, cues[0].endTimeMs)
    }

    @Test
    fun testAssCleanText() {
        val raw = "{\\b1\\pos(100,200)}Kalın metin{\\b0}\\Nİkinci satır\\hboşluk"
        val clean = SubtitleParser.cleanAssText(raw)
        assertEquals("Kalın metin\nİkinci satır boşluk", clean)
    }

    @Test
    fun testSubtitleCueTimestampParsing() {
        val ms = com.example.model.SubtitleCue.parseTimestamp("01:23.456")
        assertEquals(83456L, ms)

        val cue = com.example.model.SubtitleCue(
            id = 1,
            startTimeMs = 83456L,
            endTimeMs = 86000L,
            rawText = "Deneme",
            cleanText = "Deneme",
            customPositionEnabled = true,
            customVerticalAlign = com.example.model.SubtitleVerticalAlign.TOP,
            customHorizontalAlign = com.example.model.SubtitleHorizontalAlign.LEFT
        )
        assertEquals("01:23.456", cue.formatStartTime())
        assertTrue(cue.customPositionEnabled)
        assertEquals(com.example.model.SubtitleVerticalAlign.TOP, cue.customVerticalAlign)
    }

    @Test
    fun testColorInstantiationFromLong() {
        val colorVal: Long = 0xFFFFFFFFL
        val color = androidx.compose.ui.graphics.Color(colorVal)
        val argb = color.toArgb()
        assertEquals(-1, argb) // 0xFFFFFFFF in signed 32-bit Int is -1
    }

    @Test
    fun testAssGeneratorTimestamp() {
        // 1 hour, 23 minutes, 45 seconds, 670 ms -> 1:23:45.67
        val ms = 1 * 3600000L + 23 * 60000L + 45 * 1000L + 670L
        val formatted = com.example.parser.AssGenerator.formatAssTimestamp(ms)
        assertEquals("1:23:45.67", formatted)
    }

    @Test
    fun testSrtToAssConversionAndGeneration() {
        val srtContent = """
            1
            00:00:01,000 --> 00:00:04,500
            Birinci diyalog satırı

            2
            00:00:05,200 --> 00:00:08,000
            İkinci diyalog satırı
        """.trimIndent()

        val cues = SubtitleParser.parseSrt(srtContent)
        val style = com.example.model.SubtitleStyle(
            fontName = "Montserrat",
            fontSizeSp = 40f
        )
        val generatedAss = com.example.parser.AssGenerator.generateAss(
            title = "film_ceviri.ass",
            subtitles = cues,
            style = style
        )

        assertTrue(generatedAss.contains("[Script Info]"))
        assertTrue(generatedAss.contains("Title: film_ceviri.ass"))
        assertTrue(generatedAss.contains("[V4+ Styles]"))
        assertTrue(generatedAss.contains("Montserrat"))
        assertTrue(generatedAss.contains("[Events]"))
        assertTrue(generatedAss.contains("Dialogue: 0,0:00:01.00,0:00:04.50,Default,,0,0,0,,Birinci diyalog satırı"))
        assertTrue(generatedAss.contains("Dialogue: 0,0:00:05.20,0:00:08.00,Default,,0,0,0,,İkinci diyalog satırı"))
    }
}
