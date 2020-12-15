package com.matchi

/* Created: 2012-09-10 Mattias (mattias@tdag.se) */
class MatchingUser implements Comparable<MatchingUser> {
    User user
    int matchingValue

    int compareTo(MatchingUser other) {
        int res = 0
        if(other == null) {
            return 1
        }
        if(this) {
            res = other.matchingValue.compareTo(this.matchingValue)
            if(res == 0) {
                res = this.user.firstname.compareTo(other?.user?.firstname)
            }
        }

        return res;
    }
}
