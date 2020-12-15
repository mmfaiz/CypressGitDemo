package com.matchi

import com.matchi.facebook.FacebookUserImageMultipartFile
import com.matchi.facebook.FacebookUserTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.social.ExpiredAuthorizationException
import org.springframework.social.InvalidAuthorizationException
import org.springframework.social.facebook.api.Facebook
import org.springframework.social.facebook.api.UserOperations
import org.springframework.social.facebook.api.impl.FacebookTemplate
import org.springframework.social.facebook.api.impl.UserTemplate

class FacebookService {

    static transactional = false

    def userService

    @Value('${grails.plugins.springsocial.facebook.apiVersion}')
    String apiVersion

    UserOperations getFacebookUserTemplate(Facebook facebook) {
        return new FacebookUserTemplate(facebook, facebook.getRestTemplate())
    }

    org.springframework.social.facebook.api.User getUserProfile(Facebook facebook) {
        return getFacebookUserTemplate(facebook).getUserProfile()
    }

    def auth(def oauthToken) {
        Facebook facebook = new FacebookTemplate(oauthToken);
        facebook.setApiVersion(apiVersion)

        try {
            if(getUserProfile(facebook)?.id) {
                return facebook
            } else {
                log.error("Could not establish connection to Facebook API")
            }
        } catch(ExpiredAuthorizationException eae) {
            log.debug("Unable to authenticate facebook oauth token due to expired authorization.",eae)
            return null
        } catch(InvalidAuthorizationException iae) {
            log.debug("Unable to authenticate facebook oauth token due to invalid authorization.",iae)
            return null
        } catch(Exception e) {
            log.error("Error during facebook authentication", e)
            return null
        }

        return null
    }

    def getOrConnectUserByFacebookProfile(Facebook facebook) {

        FacebookUserTemplate facebookUserTemplate = getFacebookUserTemplate(facebook)
        def profile = facebookUserTemplate.getUserProfile()
        def user = User.findByFacebookUID(profile.id)

        // Check existing user
        if(user) {
            log.debug("Found existing facebook user: ${user.fullName()}, ${user.email}")
            return user
        }

        // Check logged in user
        user = userService.getLoggedInUser()
        if(user) {
            log.debug("Found logged in user: ${user.fullName()}, ${user.email}, connecting facebook user")
            connectWithExistingUser(user, profile, new FacebookUserImageMultipartFile(facebookUserTemplate))
            return user
        }

        // Try find matching user by email and connect
        user = User.findByEmail(profile.email)
        if(user) {
            log.debug("Found existing user to connect: ${user.fullName()}, ${user.email}")
            // User already exists but is not connected with the particular facebook user
            // lets connect and update them
            connectWithExistingUser(user, profile, new FacebookUserImageMultipartFile(facebookUserTemplate))
            return user
        }

        // No user was found, register new user based on facebook profile
        return register(profile, new FacebookUserImageMultipartFile(facebookUserTemplate))
    }

    def connectWithExistingUser(def user, def profile, def facebookProfileImage) {
        userService.addFacebookToUser(user, profile, facebookProfileImage)
    }

    def register(def facebookProfile, facebookProfileImage) {
        userService.registerUserWithFacebook(facebookProfile, facebookProfileImage)
    }

    boolean isValidProfile(Facebook facebook) {
        def profile = getUserProfile(facebook)
        return profile.email && ValidationUtils.isEmailAddress(profile.email)
    }
}
