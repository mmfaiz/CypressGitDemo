package com.matchi

import com.matchi.sportprofile.SportProfile
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(User)
@Mock([ User, SportProfile, Group, Facility, Customer, UserFavorite ])
class UserTests {

    User user
    Facility facility
    Customer customer

    @Before
    void setUp() {
        user = createUser()

        facility = new Facility(id: 1l)
        customer = new Customer(id: 1l, email: user.email, number: 123)
        new UserFavorite(user: user, facility: facility).save(failOnError: true)

        defineBeans {
            dateUtil(DateUtil) {
                grailsApplication = [config: [customer: [personalNumber: [settings: [:]]]]]
            }
        }
    }

    @Test
    void testUserCorrect() {
        def theUser = createUser()
        assert theUser.validate(['email'])
    }

    @Test
    void testUserWithUmlautEmailIncorrect() {
        def theUser = createUser()
        theUser.email ="Seröga@gmail.com"
        assert !theUser.validate(['email'])
    }

    @Test
    void testgetRoundedAverageSkillLevelReturnsCorrectSkillLevel() {
        assert user.getRoundedAverageSkillLevel() == 2
    }

    @Test
    void testgetRoundedAverageSkillLevelRoundsUp() {
        user.addToSportProfiles(new SportProfile(skillLevel: 3))

        //Total skill level set to 7 divided by 3 skill levels
        assert user.getRoundedAverageSkillLevel() == 3
    }

    @Test
    void testHasFavourite() {
        assert user.hasFavourite(facility)
    }

    @Test
    void testGetPersonalNumber() {
        assert user.getPersonalNumber() == ""

        user.birthday = new Date()

        // No configuration, assuming it will go with the default settings
        PersonalNumberSettings personalNumberSettings = new PersonalNumberSettings()

        // No country set
        assert user.getPersonalNumber() == user.birthday.format(personalNumberSettings.longFormat)

        defineBeans {
            dateUtil(DateUtil) {
                grailsApplication = [config: [
                        customer: [
                                personalNumber: [
                                        settings: [
                                                NO: [
                                                        securityNumberLength: 5,
                                                        orgPattern: /^(\d{9})$/,
                                                        longFormat: "ddMMyyyy",
                                                        shortFormat: "ddMMyy",
                                                        readableFormat: "ddmmyy"
                                                ]
                                        ]
                                ]
                        ]
                ]
            ]}
        }

        user.country = "NO"

        // NO country set ;)
        assert user.getPersonalNumber() == user.birthday.format("ddMMyyyy")


    }

    private static User createUser() {
        User user = new User()
        user.email = "email@user.com"
        user.firstname = "FIRSTNAME"
        user.lastname = "LASTNAME"

        SportProfile sp1 = new SportProfile()
        sp1.skillLevel = 2
        SportProfile sp2 = new SportProfile()
        sp2.skillLevel = 2

        user.addToSportProfiles(sp1)
        user.addToSportProfiles(sp2)

        return user
    }
}
