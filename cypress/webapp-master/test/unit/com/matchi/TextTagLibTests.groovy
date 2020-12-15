package com.matchi

import grails.converters.JSON
import grails.test.mixin.TestFor
import org.grails.plugins.sanitizer.MarkupSanitizerService
import org.junit.Before
import org.springframework.core.io.ClassPathResource

/**
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(TextTagLib)
class TextTagLibTests {

    @Before
    void setup() {
        defineBeans {
            markupSanitizerService(MarkupSanitizerService, new ClassPathResource("antisamyconfigs/antisamy-slashdot-1.4.4.xml"))
        }
    }

    void testRemoveScriptTags() {
        String dangerText = "<strong>This is some änna dåva HTML åså<script>alert('Danger!');</script></strong>"
        String safeText = "<strong>This is some änna dåva HTML åså</strong>"

        assert safeText == tagLib.toRichHTML(text: dangerText).toString()
    }

    void testRemoveInlineJavascript() {
        String dangerText = '<strong onclick="javascript:alert("Danger");">This is some änna dåva HTML åså</strong>'
        String safeText = "<strong>This is some änna dåva HTML åså</strong>"

        assert safeText == tagLib.toRichHTML(text: dangerText).toString()
    }

    void testSafeTextUnchanged() {
        String safeText = "<strong>This is some änna dåva HTML åså</strong>"

        assert safeText == tagLib.toRichHTML(text: safeText).toString()
    }

    void testExpectJsonInTag() {
        assert tagLib.EMPTY_RESPONSE == tagLib.expectJsonInTag(json: "").toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.expectJsonInTag(json: "javascript:alert('Hello');").toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.expectJsonInTag(json: "{}").toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.expectJsonInTag(json: "[]").toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.expectJsonInTag(json: "true").toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.expectJsonInTag(json: null).toString()

        List listObject = ['Victor', 'Mattias', 'Daniel']
        String listObjectAsString = '["Victor","Mattias","Daniel"]'
        assert listObjectAsString == tagLib.expectJsonInTag(json: listObject).toString()
        assert listObjectAsString == tagLib.expectJsonInTag(json: listObject as JSON).toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.expectJsonInTag(json: listObjectAsString).toString()

        Map mapObject = [ceo: 'Daniel', cto: 'Mattias', coders: ['Mattias','Sergei','Stan','Victor']]
        String mapObjectAsString = '{"ceo":"Daniel","cto":"Mattias","coders":["Mattias","Sergei","Stan","Victor"]}'
        assert mapObjectAsString == tagLib.expectJsonInTag(json: mapObject).toString()
        assert mapObjectAsString == tagLib.expectJsonInTag(json: mapObject as JSON).toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.expectJsonInTag(json: mapObjectAsString).toString()
    }

    void testForJavaScript() {
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: "").toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(data: "").toString()

        String dangerousAlert = "javascript:alert('Hello');"
        String dangerousAlertEncoded = "javascript:alert\\u0028\\u0027Hello\\u0027\\u0029\\u003b"
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: dangerousAlert).toString()
        assert dangerousAlertEncoded == tagLib.forJavaScript(data: dangerousAlert).toString()

        String emptyJSON = "{}"
        String emptyJSONEncoded = "\\u007b\\u007d"
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: emptyJSON).toString()
        assert emptyJSONEncoded == tagLib.forJavaScript(data: emptyJSON).toString()

        String emptyArray = "[]"
        String emptyArrayEncoded = "\\u005b\\u005d"
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: emptyArray).toString()
        assert emptyArrayEncoded == tagLib.forJavaScript(data: emptyArray).toString()

        String booleanTrue = "true"
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: booleanTrue).toString()
        assert booleanTrue == tagLib.forJavaScript(data: booleanTrue).toString()

        String booleanFalse = "false"
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: booleanFalse).toString()
        assert booleanFalse == tagLib.forJavaScript(data: booleanFalse).toString()

        boolean booleanFalseReal = false
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: booleanFalseReal).toString()
        assert booleanFalseReal.toString() == tagLib.forJavaScript(data: booleanFalseReal).toString()

        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(data: null).toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: null).toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript().toString()

        String redirect = "0; window.href = 'http://localhost:8080/dangerZone?=' + document.cookie"
        String redirectEncoded = "0\\u003b window.href = \\u0027http:\\u002f\\u002flocalhost:8080\\u002fdangerZone?=\\u0027 + document.cookie"
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: redirect).toString()
        assert redirectEncoded == tagLib.forJavaScript(data: redirect).toString()

        List listObject = ['Victor', 'Mattias', 'Daniel']
        String listObjectAsString = '["Victor","Mattias","Daniel"]'
        assert listObjectAsString == tagLib.forJavaScript(json: listObject).toString()
        assert listObjectAsString == tagLib.forJavaScript(json: listObject as JSON).toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: listObjectAsString).toString()

        assert "\\u005bVictor\\u002c Mattias\\u002c Daniel\\u005d" == tagLib.forJavaScript(data: listObject).toString()
        assert "\\u005b\\u0022Victor\\u0022\\u002c\\u0022Mattias\\u0022\\u002c\\u0022Daniel\\u0022\\u005d" == tagLib.forJavaScript(data: listObject as JSON).toString()
        assert "\\u005b\\u0022Victor\\u0022\\u002c\\u0022Mattias\\u0022\\u002c\\u0022Daniel\\u0022\\u005d" == tagLib.forJavaScript(data: listObjectAsString).toString()

        Map mapObject = [ceo: 'Daniel', cto: 'Mattias', coders: ['Mattias','Sergei','Stan','Victor']]
        String mapObjectAsString = '{"ceo":"Daniel","cto":"Mattias","coders":["Mattias","Sergei","Stan","Victor"]}'
        assert mapObjectAsString == tagLib.forJavaScript(json: mapObject).toString()
        assert mapObjectAsString == tagLib.forJavaScript(json: mapObject as JSON).toString()
        assert tagLib.EMPTY_RESPONSE == tagLib.forJavaScript(json: mapObjectAsString).toString()

        assert "\\u007bceo=Daniel\\u002c cto=Mattias\\u002c coders=\\u005bMattias\\u002c Sergei\\u002c Stan\\u002c Victor\\u005d\\u007d" == tagLib.forJavaScript(data: mapObject).toString()
        assert "\\u007b\\u0022ceo\\u0022:\\u0022Daniel\\u0022\\u002c\\u0022cto\\u0022:\\u0022Mattias\\u0022\\u002c\\u0022coders\\u0022:\\u005b\\u0022Mattias\\u0022\\u002c\\u0022Sergei\\u0022\\u002c\\u0022Stan\\u0022\\u002c\\u0022Victor\\u0022\\u005d\\u007d" == tagLib.forJavaScript(data: mapObject as JSON).toString()
        assert "\\u007b\\u0022ceo\\u0022:\\u0022Daniel\\u0022\\u002c\\u0022cto\\u0022:\\u0022Mattias\\u0022\\u002c\\u0022coders\\u0022:\\u005b\\u0022Mattias\\u0022\\u002c\\u0022Sergei\\u0022\\u002c\\u0022Stan\\u0022\\u002c\\u0022Victor\\u0022\\u005d\\u007d" == tagLib.forJavaScript(data: mapObjectAsString).toString()

    }
}
