package com.matchi

import org.apache.commons.lang.RandomStringUtils

class FileArchiveTagLib {

    def fileArchiveService
    def userService

    def fileArchiveURL = { attrs, body ->
        if(attrs.file) {
            out << fileArchiveService.getFileURL(attrs.file)
        }
    }

    def fileArchiveImage = { attrs, body ->
        if(attrs.file) {
            out << '<img class="img-responsive" src=\"' + fileArchiveService.getFileURL(attrs.file) + '\"/>'
        }
    }

    def fileArchiveFacilityLogoImage = { attrs, body ->
        def style = ""
        if (attrs.height) {
            def width =  "width: auto;"
            def height = "height: ${attrs.height}px;"
            style = width + height
        }

        if(attrs.file) {
            def extraClasses = attrs.class ?: ''
            out << '<img class="img-responsive ' + extraClasses + '" src=\"' + attrs.file?.getThumbnailAbsoluteURL() + '\" style="'+style+'"/>'
        } else {
            out << '<img class="img-responsive" src="' + resource(dir:"/images", file:"facility_tmp_logo.png") + '" style="'+style+'"/>'
        }
    }

    def facilityWelcomeImage = { attrs, body ->
        def facility = Facility.get(attrs.id)
        def url

        if(facility.facilityWelcomeImage) {
            url = fileArchiveService.getFileURL(facility.facilityWelcomeImage)
        } else {
            def sport = facility.sports?.findAll { it.coreSport }.sort{ Math.random() }?.getAt(0)
            url = resource(dir: 'images', file: "fac_sport_${sport.id}.jpg")
        }

        out << '<img class="img-responsive" src="'  + url + '" alt="MATCHi"/>'
    }

    def facilityWelcomeUrl = { attrs, body ->
        def facility = Facility.get(attrs.id)
        def url

        if(facility.facilityWelcomeImage) {
            url = fileArchiveService.getFileURL(facility.facilityWelcomeImage)
        } else {
            def sport = facility.sports?.findAll { it.coreSport }.sort{ Math.random() }?.getAt(0)
            if (sport) {
                url = resource(dir: 'images', file: "fac_sport_${sport.id}.jpg")
            } else {
                url = resource(dir: 'images', file: "fac_sport_1.jpg")
            }
        }

        out << url
    }

    /**
     * Displays the current logged in user profile images
     */
    def currentUserProfileImage = { attrs, body ->
        User user = userService.getLoggedInUser()
        def size = getUserProfileImageSize(attrs)
        def width =  "width: ${size.width}px;"
        def height = "height: ${size.height}px;"
        def style  = width + height

        if(user?.profileImage) {
            def userImageUrl = fileArchiveService.getFileURL(user.profileImage)

            if(attrs.useImgTag) {
                out << '<img class="img-responsive img-polaroid" src="' + userImageUrl + '" style="' + style + '"/>'
            } else {
                out << '<div class="img-polaroid user-profile-image" style="'+ style + '">' +
                       '<div class="" style="position:relative;background: url('+ userImageUrl +') no-repeat center center;background-size: contain;'+ style + '"> </div>' +
                       '</div>'
            }
        } else {
            printDefaultUserProfileImage(width, height)
        }
    }

    def userWelcomeImage = { attrs, body ->
        def user = User.get(attrs.id)
        def url

        if(user.welcomeImage) {
            url = fileArchiveService.getFileURL(user.welcomeImage)
        } else {
            url = resource(dir: 'images', file: 'cover_default.jpg')
        }

        out << '<img class="img-responsive" src="'  + url + '" alt="MATCHi"/>'
    }

    def userWelcomeUrl = { attrs, body ->
        def user = User.get(attrs.id)
        def url

        if(user.welcomeImage) {
            url = fileArchiveService.getFileURL(user.welcomeImage)
        } else {
            url = resource(dir: 'images', file: 'cover_default.jpg')
        }

        out << url
    }

    /**
     * Displays an arbitrary user profile image
     */
    def staleFileArchiveUserImage = { attrs, body ->

        def user
        def size = staleGetUserProfileImageSize(attrs)
        def width = "width: ${size.width}px;"
        def height = "height: ${size.height}px;"

        if (attrs.id) {
            user = User.get(attrs.id)
        }

        if(user?.profileImage) {
            def userImageUrl = fileArchiveService.getFileURL(user.profileImage)
            out << '<div class="img-polaroid" style="'+ width + height + '">' +
                   '<div style="background: url('+ userImageUrl +') no-repeat center center;background-size: contain;'+ width + height + '"> </div>' +
                   '</div>'

        } else {
            stalePrintDefaultUserProfileImage(width, height)
        }
    }

    private def staleGetUserProfileImageSize(attrs) {
        def defaultSize = "small"
        def sizes = [large: 132, small:45, menu: 30] // width
        def ratio = 1f // used to calculate height
        def size

        if(attrs.size && sizes[attrs.size]) {
            size = calculateUserProfileImageSize(sizes[attrs.size], ratio)
        } else {
            size = calculateUserProfileImageSize(sizes[defaultSize], ratio)
        }
        return size
    }

    /**
     * Displays an arbitrary user profile image
     */
    def fileArchiveUserImage = { attrs, body ->

        User user
        def size = getUserProfileImageSize(attrs)
        def width = "width: ${size.width}px;"
        def height = "height: ${size.height}px;"

        User.withNewSession { session ->
            if (attrs.id) {
                user = User.get(attrs.id)
            }

            if(user?.profileImage) {
                def userImageUrl = user.profileImage?.getThumbnailAbsoluteURL()
                out << '<img class="'+attrs.class+' img-responsive" src="'+ userImageUrl +'" alt="'+user.fullName().encodeAsHTML()+'"/>'

            } else {
                printDefaultUserProfileImage(width, height, attrs.class)
            }
        }

        return
    }

    private void stalePrintDefaultUserProfileImage(def width, def height, def customClass = null) {
        def avatarClass = customClass ?: "avatar-md"

        // show dummy profile image
        out << '<img class="img-responsive inline" src="'+ resource(dir:"/images", file:"avatar_default.png") +'" alt="" style="' + width + height + '"/>'
    }

    private void printDefaultUserProfileImage(def width, def height, def customClass = null) {
        def avatarClass = customClass ?: "avatar-md"

        // show dummy profile image
        out << '<img class="'+avatarClass+' img-responsive inline" src="'+ resource(dir:"/images", file:"avatar_default.png") +'" alt=""/>'
    }

    private def getUserProfileImageSize(attrs) {
        def defaultSize = "small"
        def sizes = [large: 210, small:130, menu: 30] // width
        def ratio = 0.8f // used to calculate height
        def size

        if(attrs.size && sizes[attrs.size]) {
            size = calculateUserProfileImageSize(sizes[attrs.size], ratio)
        } else {
            size = calculateUserProfileImageSize(sizes[defaultSize], ratio)
        }
        return size
    }

    private def calculateUserProfileImageSize(def width, def ratio) {
        def height = (width * ratio) as Integer
        return [width: width, height: height]
    }

    def fileArchiveAdminPreviewImage = { attrs, body ->

        if(attrs.file) {
            def parameters = [:] // remove image link parameters

            if(attrs.parameters) {
                parameters.putAll(attrs.parameters)
            }

            parameters.put("returnUrl", request.forwardURI)
            parameters.put("textId", attrs.file.textId)

            out << render(template:"/templates/filearchive/adminFormPreviewImage",
                    model: [imageFile:attrs.file, deleteAction: attrs.deleteAction, parameters:parameters])
        }

    }

    def fileArchiveImageUpload = { attrs, body ->
        def inputId = RandomStringUtils.random(10, true, false)
        def image = attrs.image
        def callback = attrs.callback
        def removeCallback = attrs.removeCallback
        def params = attrs.params

        out << render(template: "/templates/filearchive/fileArchiveImageUpload", model: [inputId : inputId,
                                                                                         callback: callback, removeCallback: removeCallback, parameters: params, image: image])
    }
}
