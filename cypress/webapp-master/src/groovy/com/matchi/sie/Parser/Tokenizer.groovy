package com.matchi.sie.Parser

import com.matchi.sie.Parser
import com.matchi.sie.Parser.Tokenizer.BeginArrayToken
import com.matchi.sie.Parser.Tokenizer.EndArrayToken
import com.matchi.sie.Parser.Tokenizer.EntryToken
import com.matchi.sie.Parser.Tokenizer.StringToken
import com.matchi.sie.Parser.Tokenizer.Token
import jline.internal.Log

import java.util.regex.Matcher
import java.util.regex.Pattern

class Tokenizer
{
    private String line
    private Scanner scanner

    Tokenizer(String line) {
        this.line = line
        this.scanner = new Scanner(line)
    }

    List<Token> tokenize()
    {
        List<Token> tokens = []
        this.checkForControlCharacters()
        String match = ""

        while(this.scanner.hasNext())
        {

            if(this.whitespace()){
                this.scanner.next()
            }
            else if(this.scanner.hasNext(Pattern.compile("#\\S*"))) { //its an entry
                match = this.scanner.next(Pattern.compile("#\\S*")).replaceAll("#", "")
                tokens.add(new EntryToken(match))
            }
            else if(this.scanner.hasNext(Pattern.compile("\\{(.*?)\\}")))  { //empty array
                this.scanner.next(Pattern.compile("\\{(.*?)\\}"))
                tokens.add(new BeginArrayToken())
                tokens.add(new EndArrayToken())
            }
            else if(this.scanner.hasNext(Pattern.compile("\\S*"))) {

                match = this.scanner.next(Pattern.compile("\\S*"))
                if(match.startsWith("{")) {
                    tokens.add(new BeginArrayToken())
                    match = match.replaceAll(Parser.BEGINNING_OF_ARRAY, "").replaceAll("\"", "")
                    tokens.add(new StringToken(match))
                }
                else if(match.endsWith("}")) {
                    match = match.replaceAll(Parser.END_OF_ARRAY, "").replaceAll("\"", "")
                    tokens.add(new StringToken(match))
                    tokens.add(new EndArrayToken())
                }
                else if(match.startsWith("\"") && !match.endsWith("\"")) // quoted string
                {
                    //match = match + " "+ this.scanner.next(Pattern.compile("\\S*"))
                    while(!match.endsWith(("\"")))
                    {
                        match = match + " "+ this.scanner.next(Pattern.compile("\\S*"))
                    }
                    match = match.replaceAll("\"", "")
                    tokens.add(new StringToken(match.replaceAll("\"", "")))


                }
                else if(match.startsWith("\"") && match.endsWith("\"")) //quoted string
                {
                    match = match.replaceAll("\"", "")
                    tokens.add(new StringToken(match.replaceAll("\"", "")))
                }
                else
                {
                    tokens.add(new StringToken(match.replaceAll("\"", "")))
                }

            }
            else if(this.endOfString()) {
                return
            }
            else {
                //error
                return
            }
        }
        return tokens
    }
    private Boolean checkForControlCharacters()
    {
        return this.line.matches("/(.*?)([\\x00-\\x08\\x0a-\\x1f\\x7f])/")
    }
    private whitespace()
    {
        return this.scanner.hasNext(Pattern.compile("[ \t]+"))
    }
    private String endOfString()
    {
        //return $this->scanner->hasTerminated();
    }
    private String removeUnnecessaryEscapes(String match)
    {
        return match.replaceAll('/\\\\([\\\\"])/', "\\\\1")
    }
}
