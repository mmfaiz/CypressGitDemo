package com.matchi

class MatchingController {

    def userService
    def matchingService

    def index( MatchingCommand cmd ) {
        log.debug("${cmd.municipality}")
        log.debug("${cmd.sport}")

        User user = (User)userService.getLoggedInUser()

        def nrOfUsersToGet = user.matchable ? 50 : 0
        def matches = matchingService.findMatchingUsers(user, nrOfUsersToGet, cmd)
        def totalMatches = matches.size()
        def allSports = Sport.realCoreSports.list()
        def sportIds = allSports.collect() { it.id }

        if(cmd.sport) {
            sportIds = [cmd.sport]
        }

        // Retrieve all munipalities and number of matchable users (minus self)
        def municipalitiesAndNumberOfUsers = Municipality.executeQuery("""
            select new map(
                 municipality as municipality, count(distinct u.id) as numUsers)
            from
                Municipality as municipality
            LEFT JOIN municipality.region
            LEFT JOIN municipality.users as u with u.matchable = true and u.email != :email
            LEFT JOIN u.sportProfiles as sp
            LEFT JOIN sp.sport as s
            WHERE s.id in (:sportIds) ${cmd.sport?"":" OR s.id is null"}
            GROUP BY municipality.id HAVING COUNT(u.id) > 0 ORDER BY municipality.name
        """,
        [email:user.email, sportIds:sportIds])

        def userCountByMunicipalities = municipalitiesAndNumberOfUsers.groupBy { it.municipality.region.id }
        def allRegions = Region.listOrderByName()

        //Number of matches to be on each page
        // Fix for matchingService doing as it does
        def end = cmd.offset + cmd.max
        if (end > matches.size()) {
            end = matches.size()
        }

        if(matches.size() > 0) {
            matches = matches.subList(cmd.offset, end)
        }

        [ allSports:allSports, userCountByMunicipalities:userCountByMunicipalities, allRegions:allRegions, user:user,
                matches:matches, cmd:cmd, totalMatches:totalMatches, end:end ]
    }
}
