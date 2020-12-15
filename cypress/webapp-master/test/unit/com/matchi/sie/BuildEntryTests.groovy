package com.matchi.sie

import com.matchi.sie.Parser.BuildEntry
import com.matchi.sie.Parser.Entry
import com.matchi.sie.Parser.Tokenizer
import com.matchi.sie.Parser.Tokenizer.Token
import org.junit.Before
import org.junit.Test

class BuildEntryTests extends GroovyTestCase{

    @Before
    void setUp() {
    }

    @Test
    void testCall() {
        String line = '#TRANS 2400 {} -200 20130101 "Foocorp expense"'
        Tokenizer tokenizer = new Tokenizer(line)
        List<Token> tokens = tokenizer.tokenize()
        Token firstToken = tokens.remove(0)
        BuildEntry buildEntry = new BuildEntry(line, firstToken, tokens, false)
        Entry entry = buildEntry.call()

        assertEquals("2400", entry.attributes.kontonr)
        assertEquals("-200", entry.attributes.belopp)
        assertEquals("20130101", entry.attributes.transdat)
        assertEquals("Foocorp expense", entry.attributes.transtext)
        assertEquals("2400", entry.attributes.kontonr)
        assertFalse(entry.attributes.kvantitet!= null)
    }

   @Test
    void testCallWithAnUnquotedZeroString()
    {
        String line = "#RAR 0 20100101 20101231"
        Tokenizer tokenizer = new Tokenizer(line)
        List<Token> tokens = tokenizer.tokenize()
        Token firstToken = tokens.remove(0)
        BuildEntry buildEntry = new BuildEntry(line, firstToken, tokens, false)
        Entry entry = buildEntry.call()

        assertEquals("0", entry.attributes.arsnr)
        assertEquals("20100101", entry.attributes.start)
        assertEquals("20101231", entry.attributes.slut)
    }
    void testCallWithShortDimensionsArray()
    {
        String line = '#TRANS 3311 {"1" "1"} -387.00'
        Tokenizer tokenizer = new Tokenizer(line)
        List<Token> tokens = tokenizer.tokenize()
        Token firstToken = tokens.remove(0)
        BuildEntry buildEntry = new BuildEntry(line, firstToken, tokens, false)
        Entry entry = buildEntry.call()

        assertEquals("3311",entry.attributes.kontonr)
        assertEquals([["dimensionsnr":"1","objektnr":"1"]],entry.attributes.objektlista)
        assertEquals("-387.00", entry.attributes.belopp)
    }

    void testCallWithLongDimensionsArray()
    {
        String line = '#TRANS 3311 {"1" "1" "6" "1"} -387.00'
        Tokenizer tokenizer = new Tokenizer(line)
        List<Token> tokens = tokenizer.tokenize()
        Token firstToken = tokens.remove(0)
        BuildEntry buildEntry = new BuildEntry(line, firstToken, tokens, false)
        Entry entry = buildEntry.call()

        assertEquals("3311", entry.attributes.kontonr )
        assertEquals([["dimensionsnr":"1","objektnr":"1"],["dimensionsnr":"6","objektnr":"1"]],entry.attributes.objektlista)
        assertEquals("-387.00", entry.attributes.belopp )
    }
    void testCallWithSimpleAttribute()
    {
        String line = '#FLAGGA 0'
        Tokenizer tokenizer = new Tokenizer(line)
        List<Token> tokens = tokenizer.tokenize()
        Token firstToken = tokens.remove(0)
        BuildEntry buildEntry = new BuildEntry(line, firstToken, tokens, false)
        Entry entry = buildEntry.call()

        assertEquals("0", entry.attributes.x)
    }
}
