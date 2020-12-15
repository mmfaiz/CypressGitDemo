package com.matchi.fortnox.v3

import com.matchi.Facility
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import com.matchi.FacilityProperty

/**
 * @author Michael Astreiko
 */
@TestFor(Fortnox3ArticleService)
//Using ControllerUnitTestMixin to enable JSON converters
@TestMixin(ControllerUnitTestMixin)
@Mock([FacilityProperty, Facility])
class Fortnox3ArticleServiceTest extends Fortnox3CommonTest {
    void setUp() {
        super.setUp()
        service.fortnox3Service = new Fortnox3Service()
        service.fortnox3Service.permitsPerSecond = 3.0d
    }

    void testList() {
        def articles = service.list(facility)

        assert articles
        assert articles.size() > 50
    }

    void testSetAndGet() {
        String description = "test description"
        FortnoxArticle article = new FortnoxArticle(Description: description)

        assert !article.ArticleNumber
        article = service.set(facility, article)

        assert article.ArticleNumber
        assert description == article.Description

        article = service.get(facility, article.ArticleNumber)
        assert description == article.Description
    }
}
