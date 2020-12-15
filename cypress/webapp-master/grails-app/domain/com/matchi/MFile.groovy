package com.matchi

import grails.util.Holders

class MFile implements Serializable {
    private static final long serialVersionUID = 12L

    public static final String OPTIMIZED_IMAGE_PREFIX = "web_"
    public static final String THUMBNAIL_PREFIX = "thumb_"

    String textId
    String originalFileName
    String contentType
    int size

    Date dateCreated
    Date lastUpdated

    static constraints = {
        originalFileName nullable: false
        textId nullable: false
        contentType nullable: true, validator: { contentType, obj ->
            if(contentType.equals("image/jpg") || contentType.equals("image/jpeg") || contentType.equals("image/gif") || contentType.equals("image/png")) {
                return true
            }

            return ['image.invalid.contentType']
        }
        size nullable: true
    }

    def getFullURL() {
        getFileArchiveService().getFileURL(this)
    }

    def getAbsoluteFileURL() {
        getFileArchiveService().getAbsoluteFileURL(this)
    }

    def getOptimizedImageFullURL() {
        getFileArchiveService().getFileURL(this, OPTIMIZED_IMAGE_PREFIX, "jpg")
    }

    def getOptimizedImageAbsoluteURL() {
        getFileArchiveService().getAbsoluteFileURL(this, OPTIMIZED_IMAGE_PREFIX, "jpg")
    }

    def getThumbnailFullURL() {
        getFileArchiveService().getFileURL(this, THUMBNAIL_PREFIX, "jpg")
    }

    def getThumbnailAbsoluteURL() {
        getFileArchiveService().getAbsoluteFileURL(this, THUMBNAIL_PREFIX, "jpg")
    }

    static mapping = {
        autoTimestamp true
    }

    private getFileArchiveService() {
        def applicationContext = Holders.applicationContext
        return applicationContext.getBean("fileArchiveService")

    }
}
