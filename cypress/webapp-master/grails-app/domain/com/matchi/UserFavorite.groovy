package com.matchi

class UserFavorite {

    User user
    Facility facility

    static belongsTo = [ User, Facility ]
}
