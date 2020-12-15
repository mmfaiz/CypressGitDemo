package com.matchi

import grails.validation.Validateable

@Validateable(nullable = true)
class MatchingCommand {
    Long sport
    Long municipality
    String country

    int max = 10
    int offset = 0
}
