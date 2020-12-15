package com.matchi

import com.matchi.excel.ExcelImportManager
import com.matchi.membership.MembershipFamily
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.hibernate.HibernateTestMixin
import grails.util.Mixin
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.createCustomer

@TestFor(ImportCustomerService)
@Mock([Customer, Group, CustomerGroup, Facility, Municipality, Region, User,  MembershipFamily])
@Mixin([HibernateTestMixin, DomainClassUnitTestMixin])
class ImportCustomerServiceTests {

    def mockSpringSecurityService
    def mockMembersFamilyService
    Facility facility
    User user

    @Before
    void setUp() {
        mockSpringSecurityService = mockFor(SpringSecurityService)
        service.springSecurityService = mockSpringSecurityService.createMock()

        mockMembersFamilyService = mockFor(MembersFamilyService)
        service.membersFamilyService = mockMembersFamilyService.createMock()

        facility = new Facility(name: "Test facility ImportCustomerServiceTests") {
            {   id = 1 }
        }
        facility.save()
        user = new User(facility: facility)
        user.save()

        mockForConstraintsTests(ImportCustomerCommand)
        mockSpringSecurityService.demand.getCurrentUser(1..10) { -> return user }

        service.importBatchSize = 100
        service.importPoolSize = 1

        defineBeans {
            dateUtil(DateUtil) {
                grailsApplication = [
                        config: [
                                customer: [
                                        personalNumber: [
                                                settings: [
                                                        NO: [
                                                                securityNumberLength: 5,
                                                                orgPattern: /^(\d{9})$/,
                                                                longFormat: "ddMMyyyy",
                                                                shortFormat: "ddMMyy",
                                                                readableFormat: "ddmmyy"
                                                        ],
                                                        SE: [
                                                                securityNumberLength: 4,
                                                                orgPattern: /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                                                                longFormat: "yyyyMMdd",
                                                                shortFormat: "yyMMdd",
                                                                readableFormat: "yymmdd"
                                                        ]
                                                ]
                                        ]
                                ]
                        ]
                ]
            }
        }
    }

    @Test
    void testParseCustomerDataReturnsCorrectData() {
        mockMembersFamilyService.demand.getAllContactsOfFacility(1) { Facility f  ->
            return []
        }

        def customerCommands = service.parseCustomerData(_createCustomerData())

        assert customerCommands.size() == 2
        assert !customerCommands[0].cmd.membershipStatusString
        assert customerCommands[1].cmd.membershipStatusString
    }

    @Test
    void testParseEmptyCustomerDataReturnsEmptyArray() {
        mockMembersFamilyService.demand.getAllContactsOfFacility(1) { Facility f  ->
            return []
        }

        def customerCommands = service.parseCustomerData([])

        assert customerCommands.size() == 0

        mockMembersFamilyService.verify()
    }

    @Test
    void testParseCustomerTypeReturnsCorrectType() {
        assert service.parseCustomerType("MAN") == Customer.CustomerType.MALE
        assert service.parseCustomerType("KVINNA") == Customer.CustomerType.FEMALE
        assert service.parseCustomerType("FÖRETAG") == Customer.CustomerType.ORGANIZATION

        assert service.parseCustomerType("NODDIN") == null
    }

    @Test
    void testImportCustomers() {
        def newCustomer = createCustomer()
        def groupService = mockFor(GroupService)
        groupService.demand.createGroup { g, f -> g }
        groupService.demand.addCustomerToGroup { g, c -> null }
        service.groupService = groupService.createMock()
        def customerService = mockFor(CustomerService)
        customerService.demand.createCustomer { cmd -> newCustomer }
        service.customerService = customerService.createMock()

        def result = service.importCustomers([[cmd: new ImportCustomerCommand(
                facilityId: 1L, number: 100L)]])

        assert result.imported.size() == 1
        assert result.imported[0] == newCustomer.id
        assert !result.existing
        assert result.group
        groupService.verify()
        customerService.verify()
    }

    @Test
    void testParseCustomerDataIfCustomerHasSameNumber() {
        Group.metaClass.static.findAllByFacility = { facility ->
            [new Group(name: "TestGroup")]}
        Customer newCustomer = createCustomer()
        mockMembersFamilyService.demand.getAllContactsOfFacility(facility.id) { Facility f  ->
            return [newCustomer]
        }
        def data = [[ number: newCustomer.number.toString(),
                  membership: null,
                  lastname: "Lastname1",
                  firstname: "Firstname1",
                  email: "email1@email.com",
                  telephone: "010-000000",
                  cellphone:  "010-000000",
                  address: "Wall Street 1",
                  zipcode: "34323",
                  city: "New York",
                  personalNumber: "990101",
                  securityNumber: "2323",
                  type: "MALE",
                  invoiceAddress: "IAddress 1",
                  invoiceTelephone: "",
                  invoiceEmail: "",
                  invoiceWeb: "",
                  country: "UA",

                  notes: ""]]

        def customerCommands = service.parseCustomerData(data);
        assert customerCommands.size() == 1
        ImportCustomerCommand cmd = customerCommands[0].cmd
        assert cmd.validate(), cmd.errors
    }

    @Test
    void testParseCustomerDataIfCustomerHasDifferentNumber() {
        Group.metaClass.static.findAllByFacility = { facility ->
            [new Group(name: "TestGroup")]}
        Customer newCustomer = createCustomer()
        mockMembersFamilyService.demand.getAllContactsOfFacility(facility.id) { Facility f  ->
            return [newCustomer]
        }
        def data = [[ number: newCustomer.number+1.toString(),
                      membership: null,
                      lastname: "Lastname1",
                      firstname: "Firstname1",
                      email: "email1@email.com",
                      telephone: "010-000000",
                      cellphone:  "010-000000",
                      address: "Wall Street 1",
                      zipcode: "34323",
                      city: "New York",
                      personalNumber: "990101",
                      securityNumber: "2323",
                      type: "MALE",
                      invoiceAddress: "IAddress 1",
                      invoiceTelephone: "",
                      invoiceEmail: "",
                      invoiceWeb: "",
                      country: "UA",
                      notes: ""]]

        def customerCommands = service.parseCustomerData(data);
        assert customerCommands.size() == 1
        ImportCustomerCommand cmd = customerCommands[0].cmd
        assert cmd.validate(), cmd.errors
    }

    @Test
    void testOverallImport() {
        List<Customer> savedCustomers = [].toList()

        mockMembersFamilyService.demand.getAllContactsOfFacility(2) { Facility f  ->
            return []
        }

        service.groupService = new GroupService() {
            //Workarounds are here because of @Transactional.
            //It's suggested to replace grails @Transactional with Spring @Transactional.
            //but I don't like an idea to fix code just for running tests
            Group createGroup(Group group, Facility facility) {
                // Workaround
                group.save()
                return group
            }

            def removeGroup(Group group) {
                // Workaround
                group.delete()
            }
        }


        service.customerService = new CustomerService() {
            @Override
            void saveToFortnox(Customer customer, Facility facility) {
                savedCustomers.add(customer)
            }
        }
        Customer agata = new Customer(facility: 1, number: 11140L, firstname: "Agata", lastname: "Kirsti", dateOfBirth: date("31/02/1975"), email: "agata2231@gmail.com").save()

        ExcelImportManager excelImportManager = new ExcelImportManager()
        def customerData = excelImportManager.parseCustomerFileContents(new File(getClass().getResource("/customer.xls").toURI()))

        def parsedData = service.parseCustomerData(customerData)
        def results = service.importCustomers(parsedData)

        assert customerData[0].number == "11140"
        assert results != null
        // Here are zeros because I cannot do something with facility.
        //This is a place for improvement later.
        assert results.imported == []
        assert results.existing == []
    }

    Date date(String dateStr) {
        new Date().parse('dd/MM/yyyy', dateStr)
    }

    static def _createCustomerData() {
        return [
                [ number: "123",
                membership: null,
                lastname: "Lastname1",
                firstname: "Firstname1",
                email: "email1@email.com",
                telephone: "010-000000",
                cellphone:  "010-000000",
                address: "Wall Street 1",
                zipcode: "34323",
                city: "New York",
                personalNumber: "990101",
                securityNumber: "2323",
                type: "MALE",
                invoiceAddress: "IAddress 1",
                invoiceTelephone: "",
                invoiceEmail: "",
                invoiceWeb: "",
                notes: ""],
                [ number: "124",
                membership: "Has it...",
                lastname: "Lastname2",
                firstname: "Firstname2",
                email: "email2@email.com",
                telephone: "010-000000",
                cellphone:  "010-000000",
                address: "Wall Street 2",
                zipcode: "34323",
                city: "New York",
                personalNumber: "990101",
                securityNumber: "2323",
                type: "MALE",
                invoiceAddress: "IAddress 2",
                invoiceTelephone: "",
                invoiceEmail: "",
                invoiceWeb: "",
                notes: ""]]
    }



}
