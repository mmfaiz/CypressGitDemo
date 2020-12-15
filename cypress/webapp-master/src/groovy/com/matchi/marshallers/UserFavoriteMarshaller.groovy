package com.matchi.marshallers

import com.matchi.UserFavorite
import grails.converters.JSON
import javax.annotation.PostConstruct

/**
 * @author Sergei Shushkevich
 */
class UserFavoriteMarshaller {

    @PostConstruct
    void register() {
        JSON.registerObjectMarshaller(UserFavorite) { UserFavorite uf ->
            marshallUserFavorite(uf)
        }
    }

    def marshallUserFavorite(UserFavorite uf) {
        [
            facility: uf.facility.id,
            user: uf.user.id
        ]
    }
}