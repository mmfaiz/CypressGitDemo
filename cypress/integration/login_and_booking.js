

// Successful login 

describe('Matchi AB Automation Testing ', function() {

  before(function()
  {
    cy.fixture('example').then(function(data) {    
    this.data=data      
    })    
  })

    it('Login using fixture', function() {

        //Visit the Matchi AB  Website
        cy.visit('/');
      
        // Navigating to "login"
      cy.get('.navbar-right > :nth-child(2) > a > span').click()   
      cy.get('#username').type(this.data.email)
      cy.get("#password").type(this.data.password)
      cy.get('#loginForm > .checkbox > label').click()      
      cy.get('#loginForm > .btn').click()
      
    })
    

   //  Booking a timeslot
/*
    it('Booking a slot', function() {

      cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
      cy.get('#inOutCourt').select('Indoors',{ force: true }) // select in or out doors courts      
      cy.get('#sport').select('Table tennis',{ force: true }) // select sports type     
      cy.get('#q').click().type("Kronan Sports Club") // search for club
      cy.get('.col-sm-2 > .btn').click() // click smash          
      cy.get('.list-inline.no-margin > :nth-child(1) > .btn').contains('17').click({force: true}) 
   //   cy.get('#slots_667 > .list-inline.no-margin > :nth-child(2) > .btn').click({ multiple: true }) // book time    
      cy.get('#se4ea4f2075bc03f00175bc04ccca0414').click({force: true}) // book slot id
      cy.get('.col-sm-8 > :nth-child(3) > .radio > label').click({ force: true}) // payment method
      cy.get('#btnSubmit').click() // submit payment
      cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking

  })*/

  it('Check first and last available slots', function() {

    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
    cy.get('#inOutCourt').select('Indoors',{ force: true }) // select in or out doors courts      
    cy.get('#sport').select('Table tennis',{ force: true }) // select sports type     
    cy.get('#q').click().type("Kronan Sports Club") // search for club
    cy.get('.col-sm-2 > .btn').click() // click smash   
    
    //cy.get('.no-margin.no-padding').first().click({force: true})
    //cy.get('.slot.free').first().click({force: true}  )
    cy.get('li').first().click({force: true})
    cy.get('.slot.free').first().click({force: true}  )
   // cy.get('["#e4ea4f2075bc03f00175bc04cce40465".free').first().click()
   cy.get('.col-sm-8 > :nth-child(3) > .radio > label').click({ force: true}) // payment method
      cy.get('#btnSubmit').click() // submit payment
      cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking

})

it('Check first and last available slots', function() {

  cy.get(':nth-child(3) > .dropdown-toggle > :nth-child(1) > .caret').click()
  
  

  cy.get('a').should('have.attr', 'href').click()

  



})
it('Check first and last available slots', function() {

  cy.get(':nth-child(1) > .text-right > .btn').click() //cancell booking
  cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn-md').click() // cancel confirmation
  cy.get('#cancelCloseBtn').click() //Your session has been cancelled


})







  })