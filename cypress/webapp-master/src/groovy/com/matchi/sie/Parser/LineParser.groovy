package com.matchi.sie.Parser

import com.matchi.sie.Parser.Tokenizer.Token
import jline.internal.Log

class LineParser
{
    private String line
    private Boolean lenient


    LineParser(line, lenient) {
        this.line = line
        this.lenient = lenient
    }

    Entry parse()
    {
        List<Token> tokens = this.tokenize(this.line)
        Token firstToken = tokens.remove(0)
        return this.buildEntry(firstToken,tokens)
    }
    List<Token> tokenize(String line)
    {
        Tokenizer tokenizer = new Tokenizer(line)
        return tokenizer.tokenize()
    }
    Entry buildEntry(Token firstToken, List<Token> tokens)
    {
        BuildEntry buildEntry = new BuildEntry(this.line, firstToken, tokens, this.lenient)
        return buildEntry.call()
    }
}
