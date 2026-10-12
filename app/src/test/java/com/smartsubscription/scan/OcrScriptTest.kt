package com.smartsubscription.scan

import org.junit.Assert.assertEquals
import org.junit.Test

class OcrScriptTest {
    @Test
    fun `the first non-Latin language picks the model`() {
        assertEquals(OcrScript.LATIN, ocrScriptFor(listOf("en", "vi", "fr")))
        assertEquals(OcrScript.JAPANESE, ocrScriptFor(listOf("en", "ja")))
        assertEquals(OcrScript.CHINESE, ocrScriptFor(listOf("zh", "ko")))
        assertEquals(OcrScript.DEVANAGARI, ocrScriptFor(listOf("hi")))
        assertEquals(OcrScript.LATIN, ocrScriptFor(emptyList()))
    }
}
