package com.matchi.sie.Parser

import com.matchi.sie.Parser.Tokenizer.BeginArrayToken
import com.matchi.sie.Parser.Tokenizer.EndArrayToken
import com.matchi.sie.Parser.Tokenizer.Token
import jline.internal.Log

class BuildEntry
{
    public String line
    public Token firstToken
    public List<Token> tokens
    public Boolean lenient

    BuildEntry(String line, Token firstToken, List<Token> tokens, Boolean lenient) {
        this.line = line
        this.firstToken = firstToken
        this.tokens = tokens
        this.lenient = lenient
    }
    Entry call() {

        if (this.firstToken.knownEntryType()) {
            return this.buildCompleteEntry()
        }
        else if(this.lenient) {
            return this.buildEmptyEntry()
        }
        else {
            this.raiseInvalidEntryError()
        }
    }
    private Entry buildCompleteEntry() {
        Entry entry = this.buildEmptyEntry()
        List<Object> attributesWithTokens = this.attributesWithTokens()
        attributesWithTokens.each { attributeWithTokens ->


            Object attr = attributeWithTokens[0]
            Object attrTokens = attributeWithTokens[1]
            if(attr instanceof String) {

                String label = attr.toString()
                entry.attributes.put (label,attrTokens)
            }
            else {

                String label = attr["name"]
                Object type = attr["type"]
                entry.attributes.put(label,[])

                if(attrTokens.size() > 0) {
                    entry.attributes.put(label,[])
                    // the attribute tokens are supplied pair wise, ie 1 2 3 4 -> [1=>2], [3=>4]
                    Integer pairs = attrTokens.size() / 2
                    if(attr["many"]==null && pairs > 1) {
                        //error
                    }
                    List<Object> tokens = []
                    List<Object> tmp = attrTokens.collate(2)
                    Map attribute = [:]
                    tmp.each { pair ->
                        Map values = [:]
                        values.put(type[0],pair[0])
                        values.put(type[1],pair[1])
                        tokens.add(values)
                    }
                    entry.attributes.put(label,tokens)

                }
                else {
                    entry.attributes.put(label,[])
                }
            }
        }
        return entry
    }
    private List attributesWithTokens()
    {
        List<Object> returnList = []
        this.lineEntryType().each { attrEntryType ->


            Token token = null

            if(tokens.size() > 0) {
                token = this.tokens.remove(0)
            }

            if(!token) {
                return
            }
            if(attrEntryType instanceof String) {
                returnList.add([attrEntryType, token.value])
            }
            else {

                if(!(token instanceof BeginArrayToken)){
                    throw new Exception("Unexpected token")
                }
                List<String> hashTokens = []
                while(token = this.tokens.remove(0))
                {
                    if(token instanceof EndArrayToken) {
                        break
                    }
                    hashTokens.add(token.value)
                }
                returnList.add([attrEntryType,hashTokens])
            }
        }
        return returnList
    }
    private Entry buildEmptyEntry()
    {
        return new Entry(this.firstToken.label())
    }
    private List lineEntryType()
    {
        return this.firstToken.entryType()
    }
    private raiseInvalidEntryError()
    {
        //throw new InvalidEntryError("Unknown entry type: " . $this->first_token->label() . "");
    }
}

