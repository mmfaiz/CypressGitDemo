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
       .invoke('val', '2020-12-01')
       .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
    cy.get('.media-heading > a').should('contain','Lundby-Biskopsgården Klubb');
      cy.wait(100)
      //choose time and slots          
      cy.get('.no-margin.no-padding').first().click()  
      cy.get('.slot.free').first().click()
     cy.get('.col-sm-8 > :nth-child(3) > .radio > label').click({ force: true}) // payment method
    cy.get('select').first().select
    cy.get('#btnSubmit').click() // submit payment
    cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking
})
it('Check if user booked successfully', function() {
  cy.get(':nth-child(3) > .dropdown-toggle > span').click()
  cy.get('.navbar-right > .open > .dropdown-menu > :nth-child(1) > a').then(elem => {
   // elem is a jQuery object
   console.log(elem.text());
   if (elem.text() == 'You have no upcoming bookings') {
       // No booking
       cy.get('.navbar-right > .open > .dropdown-menu > :nth-child(3) > a').click({force:true})
       cy.get('.page-header').should('contain','My reservation')
       cy.get(':nth-child(1) > :nth-child(1) > :nth-child(1) > .panel > .panel-body > .text-muted').should('contain','You have no upcoming bookings')    
   }
   else {
       // Have booking
       cy.get('.navbar-right > .open > .dropdown-menu > :nth-child(8) > a').click({force:true})
       cy.get('.col-sm-3 > .badge').should('be.visible')
   }
 })
})
})

