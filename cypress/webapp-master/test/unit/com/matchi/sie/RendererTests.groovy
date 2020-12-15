package com.matchi.sie

import com.matchi.sie.Document.Renderer
import org.junit.Before
import org.junit.Test

class RendererTests extends GroovyTestCase{

    Renderer renderer
    @Before
    void setUp() {
        renderer = new Renderer()
    }

    @Test
    void testReplacesInputOfTheWrongEncodingWithQuestionmark() {

        renderer.addLine("Hello ☃", [1])
        assert renderer.lines.equals(["#Hello ? 1"])
    }
}
