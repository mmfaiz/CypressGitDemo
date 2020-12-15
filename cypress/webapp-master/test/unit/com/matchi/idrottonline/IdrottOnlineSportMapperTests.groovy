package com.matchi.idrottonline

import com.matchi.Sport
import grails.test.mixin.Mock
import org.junit.After
import org.junit.Before
import org.junit.Test

@Mock([Sport])
class IdrottOnlineSportMapperTests {

    Sport tennis
    Sport badminton
    Sport squash
    Sport tabletennis
    Sport unknown
    IdrottOnlineSportMapper idrottOnlineSportMapper = new IdrottOnlineSportMapper()

    @Before
    void setUp() {
        tennis = new Sport(name: "Tennis", position: 0).save(flush: true, failOnError: true)
        badminton = new Sport(name: "Badminton", position: 1).save(flush: true, failOnError: true)
        squash = new Sport(name: "Squash", position: 2).save(flush: true, failOnError: true)
        tabletennis = new Sport(name: "Table tennis", position: 3).save(flush: true, failOnError: true)
        unknown = new Sport(name: "Unknown", position: 4).save(flush: true, failOnError: true)
    }

    @After
    void tearDown() { }

    @Test
    void testgetIdrottOnlineSportId(){

        assert "39" == idrottOnlineSportMapper.getIdrottOnlineSportId(tennis) //Tennis
        assert "1"  == idrottOnlineSportMapper.getIdrottOnlineSportId(badminton) //Badminton
        assert "60" == idrottOnlineSportMapper.getIdrottOnlineSportId(squash) //Squash
        assert "8"  == idrottOnlineSportMapper.getIdrottOnlineSportId(tabletennis) //Table tennis
        assert !idrottOnlineSportMapper.getIdrottOnlineSportId(unknown) //Unknown
    }

} 