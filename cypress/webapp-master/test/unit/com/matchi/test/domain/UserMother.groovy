package com.matchi.test.domain

import com.matchi.User

class UserMother {
    def existingUsers = []

    def createUser() {
        User user = new User()
        user.email = "test@test.com"
        user.password = "test"
        user.telephone = "031-031031"

        existingUsers << user
        return user
    }

}
