package com.matchi.sie

import com.matchi.sie.Document.Renderer
import org.junit.Before
import org.junit.Test

class ParserTests extends GroovyTestCase{

    Renderer renderer
    @Before
    void setUp() {
        renderer = new Renderer()
    }

    @Test
    void testParsesSieDataThatIncludesArrays() {

        def data =
        '''#VER "LF" 2222 20130101 "Foocorp expense"
        {
            #TRANS 2400 {} -200 20130101 "Foocorp expense"
            #TRANS 4100 {} 180 20130101 "Widgets from foocorp"
            #TRANS 2611 {} -20 20130101 "VAT"
         '''
        Parser parser = new Parser()
        def sieFile = parser.parse(data)
        def voucherEntry = sieFile.entries[0]

        assertEquals(1,sieFile.entries.size())
        assertEquals("20130101", voucherEntry.attributes.verdatum)
        assertEquals("2400", voucherEntry.entries[0].attributes.kontonr)
        assertEquals(3, voucherEntry.entries.size())
    }

    @Test
    void testHandlesLeadingWhitespace() {

        def data = '''#VER "LF" 2222 20130101 "Foocorp expense"
        {
            #TRANS 2400 {} -200 20130101 "Foocorp expense"
             #TRANS 4100 {} 180 20130101 "Widgets from foocorp"
            #TRANS 2611 {} -20 20130101 "VAT"
         '''
        Parser parser = new Parser()
        def sieFile = parser.parse(data)
        def voucherEntry = sieFile.entries[0]

        assertEquals(1,sieFile.entries.size())
        assertEquals(3, voucherEntry.entries.size())
    }
}
