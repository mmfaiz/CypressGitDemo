package com.matchi

import com.amazonaws.services.s3.model.*
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartHttpServletRequest
import sun.security.action.GetPropertyAction

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.AccessController

class S3FileService {
    static transactional = false

    def amazonWebService

    @Value('${amazon.s3.bucket}')
    String s3BucketName

    File uploadTemporaryFile(HttpServletRequest request) {
        def f = ((MultipartHttpServletRequest)request).getFile('file')
        if (f.empty) {
            throw new NullPointerException("Filen är tom")
        }
        return uploadTemporaryFile(f.inputStream)
    }

    File uploadTemporaryFile(InputStream is) {
        File ftmp = createTempFile()
        FileUtils.copyInputStreamToFile(is, ftmp)
        log.info("Stored tmp file at: ${ftmp.absolutePath}")

        uploadToS3(ftmp)
        ftmp
    }

    File downloadTemporaryFile(String filename) {
        def file = createNamedTempFile(filename)
        if (file.exists()) {
            log.info("File ${filename} found at: ${file.absolutePath}")
            return file
        }

        def path = s3Path(filename)
        if (!exists(path)) {
            log.info("File ${filename} missing on AWS path: ${path}")
            return null
        }

        log.info("File ${filename} dowloading from AWS S3")
        amazonWebService.transferManager.download(
                new GetObjectRequest(s3BucketName, path), file).waitForCompletion()
        log.info("File ${filename} dowloaded from AWS S3")
        return file
    }

    private boolean exists(String path) {
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

    private uploadToS3(File file) {
        def upload = amazonWebService.transferManager.upload(new PutObjectRequest(s3BucketName,
                s3Path(file.name), file.newInputStream(), new ObjectMetadata()
        ).withCannedAcl(CannedAccessControlList.PublicRead))
        upload.waitForUploadResult()
        log.info("Temporary file uploaded to AWS S3")
    }

    private String s3Path(filename) {
        "temporary/${filename}"
    }

    private File createTempFile() {
        def tmp = File.createTempFile("temporary-", null)
        tmp.deleteOnExit()
        tmp
    }

    private File createNamedTempFile(name) {
        def tmp = new File(new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir"))), name)
        tmp.deleteOnExit()
        tmp
    }
}
