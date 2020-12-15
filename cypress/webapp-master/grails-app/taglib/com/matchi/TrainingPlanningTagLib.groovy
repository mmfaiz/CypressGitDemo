package com.matchi

import com.matchi.activities.ActivityOccasion
import org.apache.commons.lang.StringUtils

class TrainingPlanningTagLib {
    def extraPageBreaker = { attrs, body ->
        def position = attrs.position
        def extraOccasion = attrs.extraOccasion
        def occasions = attrs.occasions
        def isPairSmallGroups = attrs.isPairSmallGroups
        if (isExtraSize(extraOccasion, position) || (!extraOccasion && isExtraSize(occasions, position, isPairSmallGroups))) {
            out << "<div id='break'></div>"
        }
    }

    def extraPageBreakerWithClass = { attrs, body ->
        def position = attrs.position
        def extraOccasion = attrs.extraOccasion
        def occasions = attrs.occasions
        def isPairSmallGroups = attrs.isPairSmallGroups
        if (isExtraSize(extraOccasion, position) || (!extraOccasion && isExtraSize(occasions, position, isPairSmallGroups))) {
            out << "<div class='break'></div>"
        }
    }

    private boolean isExtraSize(ActivityOccasion occasion, String position = "between") {
        if (!occasion) {
            return false
        }
        Integer sum = sumNumberRows(occasion)
        int limit = limitRows(position)
        return sum >= limit
    }

    private boolean isExtraSize(LinkedHashMap occasions, String position, Boolean isPairSmallGroups) {
        if (occasions.size() < 2) {
            return false
        }

        if (isBreakMoreFourLessEightOccasions(occasions) || (isPairSmallGroups && isBreakSmallGroupsOccasions(occasions))) {
            return true
        }

        def remainingOccasions = remainingOccasions(occasions)
        def extraOccasions = []
        int index = 0
        def isExtra = remainingOccasions.find {
            if (index++ > 0) {
                def firstOccasions = takeOccasions(occasions[it.key].sort { it.date })
                extraOccasions.add(maxOccasion(firstOccasions))
                def firstSum = sumNumberRows(extraOccasions[0])
                def secondSum = sumNumberRows(extraOccasions[1])
                int limit = limitRows(position)
                extraOccasions = [maxOccasion(it.value)]
                return ([firstSum, secondSum] - null).sum() >= limit
            }
            extraOccasions.add(maxOccasion(it.value))
            return false
        }
        return isExtra
    }

    private Integer sumNumberRows(ActivityOccasion occasion) {
        if (!occasion) {
            return null
        }
        def messageNumberRows = StringUtils.countMatches(occasion.message, "<p>")
        def trainersNumber = occasion.trainers?.size()
        def participantsNumber = occasion.participants?.size()
        def sum = ([messageNumberRows, trainersNumber, participantsNumber] - null).sum()
        return sum > 5 ? sum : 5
    }

    private int limitRows(String position) {
        int limit = 0
        switch (position) {
            case "inside":
                limit = 18
                break
            case "between":
                limit = 16
                break
        }
        return limit
    }

    private List takeOccasions(List occasions) {
        int indexExtraOccasion = findIndexOfExtraOccasion(occasions)
        def takeOccasions = indexExtraOccasion != -1 ? occasions.take(indexExtraOccasion) : occasions
        return takeOccasions.size() < 4 ? takeOccasions : takeOccasions.take(4)
    }

    private int findIndexOfExtraOccasion(List occasions) {
        int index = -1
        int indexOfExtraOccasion = -1
        occasions.find { item ->
            index++
            if (isExtraSize(item)) {
                indexOfExtraOccasion = index
                return true
            } else {
                return false
            }
        }
        return indexOfExtraOccasion
    }

    private ActivityOccasion maxOccasion(List occasions) {
        def map = occasions.collectEntries {[(sumNumberRows(it)): it]}
        return map.max { it.key }?.value
    }

    private LinkedHashMap remainingOccasions(LinkedHashMap occasions) {
        def remainingOccasionsMap = [:]
        occasions.each {
            int occasionsCount = 0
            def remainingOccasions = []
            int occasionsNumber = it.value.size()
            it.value.eachWithIndex { item, index ->
                if (isExtraSize(item) || (occasionsCount > 0 && occasionsCount % 4 == 0 && index + 1 < occasionsNumber)) {
                    remainingOccasions = [item]
                    occasionsCount = 1
                    return
                }
                remainingOccasions.add(item)
                occasionsCount++
            }
            remainingOccasionsMap.put(it.key, remainingOccasions)
        }
        return remainingOccasionsMap
    }

    private boolean isBreakMoreFourLessEightOccasions(LinkedHashMap occasions) {
        def firstOccasions = occasions.find { true }.value
        int size = firstOccasions.size()
        def extraOccasion = firstOccasions.find { return sumNumberRows(it) > 10 }
        return (size > 4 && size < 8) || extraOccasion
    }

    private boolean isBreakSmallGroupsOccasions(LinkedHashMap occasions) {
        int sum = 0
        occasions.each {
            if (it.value.size() <= 4) {
                def isExtraOccasion = it.value.find { return sumNumberRows(it) > 7 }
                if (!isExtraOccasion) {
                    sum += it.value.size()
                }
            }
        }
        return sum > 0 && sum <= 8
    }
}
