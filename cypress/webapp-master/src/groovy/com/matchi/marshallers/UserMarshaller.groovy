package com.matchi.marshallers

import com.matchi.User
import grails.converters.JSON

import javax.annotation.PostConstruct

class UserMarshaller {

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(User) { User user ->
            marshallUser(user)
        }
    }

    def marshallUser(User user) {
        [
                id: user.id,
                email: user.email,
                firstname: user.firstname,
                lastname: user.lastname,
                address: user.address ?:"",
                zipcode: user.zipcode ?:"",
                city: user.city ?:"",
                municipality: user.municipality ?: "",
                telephone: user.telephone ?:"",
                country: user.country ?:"",
                description: user.description ?:"",
                birthday: user.birthday ?:"",
                gender: user.gender ? user.gender.name : "",

                profileImageUrl: user.profileImage ?
                        user.profileImage.getAbsoluteFileURL() : "",

                welcomeImage: user.welcomeImage ?
                        user.welcomeImage.getAbsoluteFileURL() : "",

                dateAgreedToTerms: user.dateAgreedToTerms ?:"",
        ]
    }
}
