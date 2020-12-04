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
        cy.get('a').then(link => {cy.request(link.prop('href')).its('status').should('eq', 200);});
        // Navigating to "login"
      cy.get('.navbar-right > :nth-child(2) > a > span').click()   
      cy.get('#username').type('abc001@gmail.com')
      cy.get('#password').type('1234567')
      cy.get('#loginForm > .checkbox > label').click()      
      cy.get('#loginForm > .btn').click()
      cy.get('.navbar-brand').contains('matchi',{ matchCase: false })
    })
  it('Book first available time slots using Gift Card', function() {
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
    //choose indoors, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#showDate')
       .invoke('val', '2020-12-05')
       .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').click();
    cy.get('.media-heading > a').should('contain','Lundby-Biskopsgården Klubb');
      cy.wait(100)
      //choose time and slots          
      cy.get('.no-margin.no-padding').first().click()  
      cy.get('.slot.free').first().click()
     cy.get('.col-sm-8 > :nth-child(3) > .radio > label').click({ force: true}) // payment method
    cy.get('select').first().select
    cy.get('[class="btn btn-md btn-success"]').click() // submit payment

    cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking
})

it('Check if user booked successfully', function() {
  
  cy.get(':nth-child(3) > .dropdown-toggle > :nth-child(1)').click()
    .get('a[href*="bookings"]').contains('Show all my bookings').click({ force: true }) // SHow all bookings

  cy.get(':nth-child(1) > .text-right > .btn').click({force:true})
  cy.get('.btn-rnd.btn-danger').click()
  //cy.get('#cancelCloseBtn').click()
   
})
})