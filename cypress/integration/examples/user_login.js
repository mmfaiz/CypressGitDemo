

// Successful login 

describe('Successfull Login ', function() {

  before(function()
  {
    cy.fixture('example').then(function(data) {    
    this.data=data      
    })    
  })

    it('Login with fixture', function() {

        //Visit the Matchi AB  Website
        cy.visit('/');
      
        // Navigating to "login"
      cy.get('.navbar-right > :nth-child(2) > a > span').click()   
      cy.get('#username').type(this.data.email)
      cy.get("#password").type(this.data.password)
      cy.get('#loginForm > .checkbox > label').click()      
      cy.get('#loginForm > .btn').click()
      
    })

    it('book a slot', function() {

      cy.get('.col-sm-4 > :nth-child(4) > .btn').click()

      cy.get('#inOutCourt').select('Indoors',{ force: true })
      
      
    
    cy.get('#q').click().type("Kronan Sports Club")
    cy.get('.col-sm-2 > .btn').click()

    //cy.contains('12').click({force: true})
    cy.get('.list-inline.no-margin > :nth-child(1) > .btn').contains('12').click({ waitForAnimations: false })
    cy.wait(1000)
    //cy.get('#se4ea4f2075bc03f00175bc04cbf9020e')
    cy.get('#se4ea4f2075bc03f00175bc04cbf9020e').click({force: true})
    cy.get('.col-sm-8 > :nth-child(3) > .radio > label').click({ waitForAnimations: false })
    cy.get('#btnSubmit').click()
    cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()





  })
  })