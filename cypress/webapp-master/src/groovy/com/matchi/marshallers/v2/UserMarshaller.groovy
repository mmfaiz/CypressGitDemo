package com.matchi.marshallers.v2

import com.matchi.User

class UserMarshaller {

    static void register(json) {
        // Standard Node marshall
        json.registerObjectMarshaller(User) { User user ->
            marshallUser(user)
        }
    }

    static marshallUser(User user) {
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
                favouriteFacilities: user.favourites.collect{ it.facility.id } ?:[],
                language: user.language
        ]
    }
}
