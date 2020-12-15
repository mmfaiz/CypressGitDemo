package com.matchi.sie.Parser.Tokenizer

import com.matchi.sie.Parser
import com.matchi.sie.Parser.EntryTypes
import jline.internal.Log

class Token
{
    public String value = ""

    Token() {
    }
    Token(value) {
        this.value = value
    }
    Boolean knownEntryType()
    {
        return EntryTypes.ENTRY_TYPES.containsKey(this.label())
    }
    List entryType()
    {
        return EntryTypes.ENTRY_TYPES.get(this.label())
    }
    String label()
    {
        return this.value.replaceAll("/^#/","").toLowerCase()
    }
}
