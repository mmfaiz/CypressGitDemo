package com.matchi.sie.Parser

import com.matchi.sie.Parser.Entry

class SieFile {

    public List<Entry> entries = []
    List<Entry> entriesWithLabel(String label)
    {
        List entriesWithLabel = []
        this.entries.each { entry ->
            if(entry.label == label) {
                entriesWithLabel.add(entry)
            }
        }
        return entriesWithLabel
    }
}
