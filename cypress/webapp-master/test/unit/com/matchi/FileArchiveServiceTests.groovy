package com.matchi

import grails.test.mixin.TestFor
import groovy.mock.interceptor.MockFor
import org.junit.Before
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

@TestFor(FileArchiveService)
class FileArchiveServiceTests {

    def fileArchiveService
    def grailsApplication

    @Before
    void setUp() {

        fileArchiveService = [transferFileToArchive: {a, b, x, y, w, h -> return "" }] as FileArchiveService

        mockDomain(MFile)

        String.metaClass.encodeAsMD5 = {
            org.codehaus.groovy.grails.plugins.codecs.MD5Codec.encode(delegate)
        }

        fileArchiveService.grailsApplication = grailsApplication
    }

    void testStoreFileReturnsMFile() {
        def file = fileArchiveService.storeFile(createMockFile())
        assertNotNull(file)
    }

    void testStoreFileReturnSavedMFile() {
        def file = fileArchiveService.storeFile(createMockFile())
        assertNotNull(file.id)
    }

    void testStoreFileReturnMFileGeneratedTextId() {
        def file = fileArchiveService.storeFile(createMockFile())
        assertNotNull(file.textId)
    }

    void testStoreFileReturnMFileContentType() {
        def file = fileArchiveService.storeFile(createMockFile())
        assertEquals("image/gif", file.contentType)
    }

    void testStoreFileReturnMFileWithOriginalFileName() {
        def mockFile = createMockFile()
        def file = fileArchiveService.storeFile(mockFile)
        assertEquals("image.gif", file.originalFileName)
    }

    void testStoreFileThrowsExceptionWithOriginalNullAsFileName() {
        try {
            fileArchiveService.storeFile(createMockFile(null))
        } catch(IllegalArgumentException e) {
            return
        }
        fail("Should throw exception")
    }

    void testStoreFileWithWrongContentTypeThrowsException() {
        def mockMultipartFileControl = new MockFor(MultipartFile)
        def actualOutputPath


        mockMultipartFileControl.demand.getOriginalFilename(1..10) { 'test.pdf' }
        mockMultipartFileControl.demand.getContentType(1..1) { 'image/pdf' }
        mockMultipartFileControl.demand.getSize(1..1) { 400000 }

        mockMultipartFileControl.demand.transferTo { File f ->
            actualOutputPath = f.canonicalPath
        }

        def multipartFile = mockMultipartFileControl.proxyInstance()

        shouldFail(IllegalStateException) {
            fileArchiveService.storeFile(multipartFile)
        }
    }


    MultipartFile createMockFile() {
        return createMockFile("image.gif")
    }

    MultipartFile createMockFile(def originalFileName) {
        return new MockMultipartFile(
                'imageFile',
                originalFileName,
                'image/gif',
                new byte[0])
    }
}
