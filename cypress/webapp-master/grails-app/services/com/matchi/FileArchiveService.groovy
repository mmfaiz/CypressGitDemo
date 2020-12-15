package com.matchi

import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import groovyx.gpars.GParsPool
import org.apache.commons.io.FilenameUtils
import org.joda.time.DateTime
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletResponse
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.beans.factory.annotation.Value

// TODO: rename service/methods, since it will be used to store/get all files (from AWS S3)
class FileArchiveService {

    static transactional = false

    def grailsApplication
    def amazonWebService
    def imageService

    @Value('${amazon.s3.bucket}')
    String s3BucketName

    @Value('${exports.rootPath}')
    String exportsRootPath

    def getFileURL(MFile file, String prefix = null, String ext = null) {
        def baseUrl = grailsApplication.config.archive.baseUrl
        return baseUrl + resolveCDNPathWithName(file, prefix, ext)
    }

    def getAbsoluteFileURL(MFile file, String prefix = null, String ext = null) {
        return getFileURL(file, prefix, ext) // same when using CDN
    }

    /**
     * The SHIT!
     * @param file
     * @return
     */
    def storeFile(MultipartFile file, Integer cropX = null, Integer cropY = null,
                  Integer cropWidth = null, Integer cropHeight = null) {

        if(file.originalFilename == null || file.originalFilename == "") {
            throw new IllegalArgumentException("OriginalFileName must not be null")
        }

        def mfile = new MFile()

        mfile.textId = generateTextId(file)
        mfile.originalFileName = file.originalFilename
        mfile.contentType = file.contentType
        mfile.size = file.size

        if(mfile.hasErrors() || !mfile.save()) {
            throw new IllegalStateException("Unable to save file: " + mfile.errors);
        }

        transferFileToArchive(file, mfile, cropX, cropY, cropWidth, cropHeight)

        return mfile;
    }

    def removeFile(MFile file) {
        // TODO Implement S3 removal if needed
    }

    String storeExportedFile(File file) {
        def pathPrefix = "$exportsRootPath/${new Date().format('yyyy/MM')}"
        def path = "$pathPrefix/$file.name"
        while(exists(path)) {
            path = "$pathPrefix/${makeUniqueFileName(file.name)}"
        }

        amazonWebService.transferManager.upload(
                new PutObjectRequest(s3BucketName, path, file)
                .withCannedAcl(CannedAccessControlList.Private))
                .waitForUploadResult()

        path
    }

    File downloadFile(String path) {
        if (!exists(path)) {
            return null
        }

        def file = File.createTempFile("download-", null)
        amazonWebService.transferManager.download(
                new GetObjectRequest(s3BucketName, path), file).waitForCompletion()
        file
    }

    void updateUploadedImages() {
        log.info("Updating uploaded images")
        def files = MFile.list()
        AtomicInteger count = new AtomicInteger(0)

        // NOTE! This is a one time job executed for maintenance/migrations reason and therefore using hardcoded 15 as numberOfThreads.
        GParsPool.withPool(15) {
            files.eachParallel { MFile file ->
                count.incrementAndGet()

                try {
                    def filePath = resolveCDNPathWithName(file)

                    log.info("Updating image (${count}/${files.size()}): ${file.textId} => ${filePath}")

                    if (exists(filePath)) {
                        if (!exists(resolveCDNPathWithName(file, MFile.OPTIMIZED_IMAGE_PREFIX, ".jpg"))) {
                            uploadOptimizedImageToS3(file)
                        }
                        if (!exists(resolveCDNPathWithName(file, MFile.THUMBNAIL_PREFIX, ".jpg"))) {
                            uploadThumbnailToS3(file)
                        }
                    }
                } catch(e) {
                    log.info("Could not update image ${file.textId}")
                }
            }
        }
    }

    boolean exists(String path) {
        try {
            def meta = amazonWebService.s3.getObjectMetadata(s3BucketName, path)
            return meta.contentLength > 0
        } catch (AmazonS3Exception e) {
            if (e.statusCode == HttpServletResponse.SC_NOT_FOUND) {
                return false
            } else {
                throw e
            }
        }
    }

    public def transferFileToArchive(MultipartFile file, MFile mFile, Integer cropX = null,
                                     Integer cropY = null, Integer cropWidth = null, Integer cropHeight = null) {
        uploadToS3(mFile, file.inputStream, false)
        uploadOptimizedImageToS3(mFile)
        uploadThumbnailToS3(mFile, cropX, cropY, cropWidth, cropHeight)
    }

    private void uploadToS3(MFile mFile, InputStream inputStream, boolean async = true,
                            String filePrefix = "", String ext = null) {
        def cdnPath = resolveCDNPath(mFile)

        def extension = ext ?: FilenameUtils.getExtension(mFile.originalFileName)
        String fullFilePath = "${cdnPath}/${filePrefix}${mFile.textId}" + (extension ? ".${extension}" : "")

        def upload = amazonWebService.transferManager.upload(new PutObjectRequest(s3BucketName,
                fullFilePath, inputStream, new ObjectMetadata(contentType: mFile.contentType)
        ).withCannedAcl(CannedAccessControlList.PublicRead))
        if (!async) {
            upload.waitForUploadResult()
        }
    }

    private void uploadOptimizedImageToS3(MFile mFile) {
        def img = imageService.createOptimizedImage(new URL(mFile.absoluteFileURL))
        uploadToS3(mFile, img.newInputStream(), false, MFile.OPTIMIZED_IMAGE_PREFIX,
                FilenameUtils.getExtension(img.name))
        img.delete()
    }

    private void uploadThumbnailToS3(MFile mFile, Integer cropX = null,
                                     Integer cropY = null, Integer cropWidth = null, Integer cropHeight = null) {
        def thumb = imageService.createThumbnail(new URL(mFile.optimizedImageAbsoluteURL),
                cropX, cropY, cropWidth, cropHeight)
        uploadToS3(mFile, thumb.newInputStream(), false, MFile.THUMBNAIL_PREFIX,
                FilenameUtils.getExtension(thumb.name))
        thumb.delete()
    }

    private resolveCDNPath(MFile file) {
        def rootPath = grailsApplication.config.archive.rootPath ?: "archive"
        StringBuilder sb = new StringBuilder(rootPath)
        sb.append("/")
        sb.append(new DateTime(file.dateCreated).toString("yyyy/MM"))

        return sb.toString()
    }

    private resolveCDNPathWithName(MFile file, String prefix = null, String ext = null) {
        StringBuilder sb = new StringBuilder(resolveCDNPath(file))
        sb.append("/")
        if (prefix) {
            sb.append(prefix)
        }
        sb.append(file.textId)
        def extension = ext ?: FilenameUtils.getExtension(file.originalFileName)
        if (extension) {
            sb.append(".").append(extension)
        }
        return sb.toString()
    }

    private def generateTextId(MultipartFile file) {
        def dateString = String.valueOf(System.currentTimeMillis())
        def fileName   = file.originalFilename

        return (dateString + fileName).encodeAsMD5()
    }

    private String makeUniqueFileName(String filename) {
        def baseName = FilenameUtils.getBaseName(filename)
        def ext = FilenameUtils.getExtension(filename)
        "$baseName-${System.currentTimeMillis()}${ext ? '.' + ext : ''}"
    }
}
