package com.matchi.idrottonline

import com.matchi.*
import com.matchi.idrottonline.commands.OrganisationCommand
import com.matchi.idrottonline.commands.OrganisationsCommand
import com.matchi.idrottonline.commands.PersonCommand
import com.matchi.idrottonline.commands.SportCommand
import com.matchi.idrottonline.commands.SportsCommand
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.test.MockUtils

@TestMixin(GrailsUnitTestMixin)
class PersonCommandTests {

    PersonCommand person

    @Before
    public void setUp() {
        MockUtils.mockCommandObject PersonCommand
        person = createPerson()
    }

    @After
    public void tearDown() {

    }

    @Test
    void testPersonalIdentityNumberValidWhenInAllowedRange(){
        // Range allowed in IdrottOnline is between 1800-2100.
        person.personalIdentityNumber = "19820817-5599"
        assert person.validate()
    }

    @Test
    void testPersonalIdentityNumberInvalidWhenUnderMinimumAllowedRange(){
        // Min allowed in IdrottOnline is 1800XXXX-XXXX.
        person.personalIdentityNumber = "17900817-5599"
        assert !person.validate()
    }

    @Test
    void testPersonalIdentityNumberInvalidWhenOverMaximumAllowedRange(){
        // Max allowed in IdrottOnline is 2100XXXX-XXXX.
        person.personalIdentityNumber = "21010817-5599"
        assert !person.validate()
    }

    @Test
    void testPersonalIdentityNumberInvalidWhenEndsWithSpace(){
        // IdrottOnline does not accept personal identity numbers ending with space (batch gets corrupt).
        person.personalIdentityNumber = "19460329-1032 "
        assert !person.validate()
    }

    @Test
    void testMissingPersonalIdentityNumberIsInvalid(){
        person.personalIdentityNumber = ""
        assert !person.validate()
    }

    @Test
    void testMissingPersonalIdentityNumberIsNull(){
        person.personalIdentityNumber = null
        assert !person.validate()
    }

    @Test
    void testPersonalIdentityNumberInvalidWhenMoreThan4DigitsSecurityNumber(){
        person.personalIdentityNumber = "20000501-0005015847"
        assert !person.validate()
    }

    @Test
    void testPersonalIdentityNumberInvalidWhenLessThan4DigitsSecurityNumber(){
        person.personalIdentityNumber = "19490202-73"
        assert !person.validate()
    }

    static PersonCommand createPerson() {
        PersonCommand person = new PersonCommand()
        person.createDate = new Date().toString()
        person.changeDate = new Date().toString()
        person.optionType = PersonCommand.OptionType.UPDATE.toString()
        person.birthDate = "Test"
        person.externalIdentification = "123"
        person.firstName = "John"
        person.lastName = "Doe"
        person.gender = "Male"
        person.organisations = createOrganisationsCommand()
        person
    }

    static List<OrganisationsCommand> createOrganisationsCommand(){
        List<OrganisationsCommand> organisations = new ArrayList<OrganisationsCommand>()
        OrganisationCommand organisation = new OrganisationCommand(
                        optionType: OrganisationCommand.OptionType.UPDATE.toString(),
                        externalOrganisationIdentification: "123",
                        organisationIdentityNumber: "123",
                        sports: createSportsCommand())

        organisations << new OrganisationsCommand(organisation: organisation)
        organisations
    }

    static List<SportsCommand> createSportsCommand(){
        List<SportsCommand> sports = new ArrayList<SportsCommand>()
        SportCommand sport = new SportCommand(
                optionType: SportCommand.OptionType.UPDATE.toString(),
                isActive: "true",
                sportID: 39)

        sports << new SportsCommand(sport: sport)
        sports.toList()
    }
}
