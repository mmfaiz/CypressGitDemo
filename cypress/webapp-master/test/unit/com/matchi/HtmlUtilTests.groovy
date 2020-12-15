package com.matchi

import org.junit.Test

class HtmlUtilTests {

    @Test
    void testCloseInlineElements() {
        def html1 = "<p><u><i></i></u>Some text... <img src=\"http://example.com/image.png\"><br></p>"
        def html2 = "<p><u><i></i></u>Some text... <img src=\"http://example.com/image.png\"></p>"
        def html3 = "<p><u><i></i></u>Some text... <br></p>"
        def html4 = "<p><u><i></i></u>Some text... <br><br></p>"
        def html5 = "<p><u><i></i></u>Some text... <br/><br></p>"

        assert HtmlUtil.closeInlineElements(html1) == "<p><u><i></i></u>Some text... <img src=\"http://example.com/image.png\"/><br/></p>"
        assert HtmlUtil.closeInlineElements(html2) == "<p><u><i></i></u>Some text... <img src=\"http://example.com/image.png\"/></p>"
        assert HtmlUtil.closeInlineElements(html3) == "<p><u><i></i></u>Some text... <br/></p>"
        assert HtmlUtil.closeInlineElements(html4) == "<p><u><i></i></u>Some text... <br/><br/></p>"
        assert HtmlUtil.closeInlineElements(html5) == "<p><u><i></i></u>Some text... <br/><br/></p>"

        def sameHtml = "<p><u><i></i></u>Some text... <br/></p>"
        assert HtmlUtil.closeInlineElements(sameHtml) == "<p><u><i></i></u>Some text... <br/></p>"
    }

    @Test
    void testEscapeAmpersands() {
        def html = "<p>Some & test & text</p>"
        def result = "<p>Some &amp; test &amp; text</p>"

        assert HtmlUtil.escapeAmpersands(html) == result
        assert HtmlUtil.escapeAmpersands(result) == result
    }
}
