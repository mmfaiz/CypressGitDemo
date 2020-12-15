package com.matchi

interface IRequired {

    /**
     * Checks if a user fulfills requirement profile.
     * Validates to true if facility has no requirement profiles.
     * @param user
     * @param facility
     * @return
     */
    boolean checkUser(User user)
}