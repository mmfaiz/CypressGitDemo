package com.matchi

import com.matchi.LocaleHelper.Country

class FrontEndMessage {

    String name
    String baseId

    String htmlContent
    String cssCode

    Date publishDate
    Date endDate

    static hasMany = [countries: Country, images: MFile]

    static mapping = {
        countries joinTable: [name: 'front_end_message_countries', key: 'front_end_message_id', column: 'country']
    }

    static constraints = {
        htmlContent nullable: true
        cssCode nullable: true
    }

    FrontEndMessage() {
        if (!this.baseId) {
            this.baseId = "base"+UUID.randomUUID().toString().substring(0,7)
        }
    }

    Map<String, MFile> getImagesList() {
        Map<String, MFile> imagesList = new HashMap<String, MFile>()
        this.images.each {
            imagesList.put(it.originalFileName.trim().stripIndent().replaceAll("\\s+", "_").toLowerCase(), it)
        }
        return imagesList
    }
}
