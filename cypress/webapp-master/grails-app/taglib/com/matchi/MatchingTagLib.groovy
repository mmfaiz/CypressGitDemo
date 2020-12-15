package com.matchi

class MatchingTagLib {

    def matchingService
    def userService

    def getRandomMatches = { attr, body ->
        def matches = matchingService.findMatchingUsers(userService.getLoggedInUser(), 20)

        if (attr.number && matches) {
            matches = matchingService.randomizeMatchingResult( matches, (attr.number).toInteger() )
        }

        out << render(template:"/templates/matching/random", model: [ matches: matches ])
    }
}
