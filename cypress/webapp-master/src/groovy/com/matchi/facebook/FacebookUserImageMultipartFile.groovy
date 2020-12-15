package com.matchi.facebook

import org.springframework.social.facebook.api.Facebook
import org.springframework.web.multipart.MultipartFile
import org.springframework.social.facebook.api.ImageType

/**
 * Created by IntelliJ IDEA.
 * User: mattias
 * Date: 2012-01-20
 * Time: 09:30
 * To change this template use File | Settings | File Templates.
 */
class FacebookUserImageMultipartFile implements MultipartFile {

    byte[] fileContents
    String userId

    FacebookUserImageMultipartFile(FacebookUserTemplate facebookUserTemplate) {
        this(facebookUserTemplate.getUserProfileImage(ImageType.LARGE), facebookUserTemplate.getUserProfile().id)
    }

    FacebookUserImageMultipartFile(byte[] fileContents, String userId) {
        this.fileContents = fileContents
        this.userId = userId
    }

    String getName() {
        return "facebook-large-${userId}"
    }

    String getOriginalFilename() {
        return getName()
    }

    String getContentType() {
        return "image/jpg"
    }

    boolean isEmpty() {
        return fileContents.length == 0
    }

    long getSize() {
        return fileContents.length
    }

    byte[] getBytes() {
        return fileContents
    }

    InputStream getInputStream() {
        return new ByteArrayInputStream(fileContents)
    }

    void transferTo(File file) {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(fileContents)
        fos.close()
    }

}
