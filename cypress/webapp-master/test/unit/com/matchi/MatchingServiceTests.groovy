package com.matchi

import grails.test.mixin.TestFor

/**
 * @author Sergei Shushkevich
 */
@TestFor(MatchingService)
class MatchingServiceTests {

    void testRandomizeMatchingResult() {
        def matches = [new MatchingUser(), new MatchingUser(), new MatchingUser()]
        assert service.randomizeMatchingResult(matches, 1).size() == 1
        assert service.randomizeMatchingResult(matches, 2).size() == 2
        assert service.randomizeMatchingResult(matches, 3).size() == 3
        assert service.randomizeMatchingResult(matches, 4).size() == 3
    }

    void testGetCriteriasCombinations() {
        def result = service.getCriteriasCombinations([
                MatchingCriteria.SPORT, MatchingCriteria.SKILL, MatchingCriteria.REGION])
        assert result.size() == 7
        assert result[0].size() == 1
        assert result[0][0] == MatchingCriteria.REGION
        assert result[1].size() == 1
        assert result[1][0] == MatchingCriteria.SKILL
        assert result[2].size() == 1
        assert result[2][0] == MatchingCriteria.SPORT
        assert result[3].size() == 2
        assert result[3].contains(MatchingCriteria.SKILL)
        assert result[3].contains(MatchingCriteria.REGION)
        assert result[4].size() == 2
        assert result[4].contains(MatchingCriteria.SPORT)
        assert result[4].contains(MatchingCriteria.REGION)
        assert result[5].size() == 2
        assert result[5].contains(MatchingCriteria.SPORT)
        assert result[5].contains(MatchingCriteria.SKILL)
        assert result[6].size() == 3
        assert result[6].contains(MatchingCriteria.SPORT)
        assert result[6].contains(MatchingCriteria.SKILL)
        assert result[6].contains(MatchingCriteria.REGION)

        assert !service.getCriteriasCombinations([])

        result = service.getCriteriasCombinations([MatchingCriteria.SKILL])
        assert result.size() == 1
        assert result[0].size() == 1
        assert result[0][0] == MatchingCriteria.SKILL
    }
}