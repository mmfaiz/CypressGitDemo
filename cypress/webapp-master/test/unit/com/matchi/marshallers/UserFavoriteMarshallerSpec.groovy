package com.matchi.marshallers

import com.matchi.Facility
import com.matchi.User
import com.matchi.UserFavorite
import spock.lang.Specification

/**
 * @author Sergei Shushkevich
 */
class UserFavoriteMarshallerSpec extends Specification {

    void testMarshallClassActivityWatch() {
        def facility = new Facility()
        facility.id = 100L
        def user = new User()
        user.id = 200L
        def fav = new UserFavorite(user: user, facility: facility)

        when:
        def result = new UserFavoriteMarshaller().marshallUserFavorite(fav)

        then:
        result.facility == 100L
        result.user == 200L
    }
}