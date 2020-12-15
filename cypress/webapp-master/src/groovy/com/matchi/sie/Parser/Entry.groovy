package com.matchi.sie.Parser

class Entry {

    public String label
    public Map attributes = [:]
    public List<Entry> entries = []
    Entry(String label)
    {
        this.label = label
        this.attributes = [:]
        this.entries = []
    }
}
