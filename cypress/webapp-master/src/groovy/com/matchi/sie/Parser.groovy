package com.matchi.sie

import com.matchi.sie.Document.CP437Encoding
import com.matchi.sie.Parser.*
import jline.internal.Log

import java.util.regex.Matcher
import java.util.regex.Pattern

class Parser
{
    static final BEGINNING_OF_ARRAY = "\\{"
    static final END_OF_ARRAY = "\\}"
    static final ENTRY = "#"
    private options

    Parser() {
        super()
    }
    Parser(options) {
        this.options = options
    }

    SieFile parseSieFileContents(String fileContents) {
        String data = CP437Encoding.convertFromCP437ToUTF8(fileContents)
        return this.parse(data)
    }
    SieFile parse(String data)
    {
        SieFile sieFile = new SieFile()
        Entry current = null
        List<String> lines = data.split('\n')
        Boolean array = false

        lines.each { line ->
            line = line.trim()
            if(Pattern.compile(BEGINNING_OF_ARRAY).matcher(line).lookingAt()) {
                array = true
                current = sieFile.entries[sieFile.entries.size()-1]
            }
            else if(Pattern.compile(END_OF_ARRAY).matcher(line).lookingAt()) {
                array = false
            }
            else if(array && Pattern.compile(ENTRY).matcher(line).lookingAt()) {
                current.entries.add(this.parseLine(line))
            }
            else if(Pattern.compile(ENTRY).matcher(line).lookingAt()) {
                sieFile.entries.add(this.parseLine(line))
            }
        }
        return sieFile
    }
    /**
     * @param $line
     * @throws Exception
     * @return \sie\parser\Entry
     */
    private parseLine(String line)
    {
        LineParser lineParser = new LineParser(line, this.lenient())
        Entry entry = lineParser.parse()
        return entry
    }
    private lenient()
    {
        return this.options?.containsKey('lenient') ? this.options["lenient"] : null
    }
}
