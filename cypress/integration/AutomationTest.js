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
      cy.get('#username').type('abc002@gmail.com')
      cy.get("#password").type('1234567')
      cy.get('#loginForm > .checkbox > label').click()      
      cy.get('#loginForm > .btn').click({ force: true })    
    })
    

    it('Book first available time slots using Saved Card, and cancel a booking', function() {  
      cy.get(':nth-child(1) > .badge').then(($btn) => {

        // store the label's text
        const BookingNumber = $btn.text()
   
        // Book
         cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
        
        //choose indoors, sport, club's  name, then smash
        cy.get('#inOutCourt').select('Indoors',{ force: true })
        cy.get('#showDate')
          .invoke('val', '2020-12-11')
          .trigger('change')
        cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
        cy.get('.col-sm-2 > .btn').eq(0).click();
        cy.get('.media-heading > a').should('contain','Lundby-Biskopsgården Klubb');
    
        cy.wait(100)
    //choose time and slots          
        cy.get('.no-margin.no-padding').first().click()  
        cy.get('.slot.free').first().click({force: true})
        
      //  cy.get('[class="trailingSlotSelector"]')
      //    .select('12:00')    
        
        cy.get('[class="btn btn-md btn-success"]').click() // submit payment
    
        cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking
        // compare the two texts
        // and make sure they are different
        cy.get(':nth-child(1) > .badge').should(($btn2) => {
           expect($btn2.text()).not.to.eq(BookingNumber)
        })
        
        cy.get(':nth-child(3) > .dropdown-toggle > :nth-child(1)').click()
        .get('a[href*="bookings"]').contains('Show all my bookings').click({ force: true }) // SHow all bookings
        
        cy.get(':nth-child(1) > :nth-child(1) > :nth-child(1) > .panel > .panel-heading > .row > .col-sm-3').click()
          .get('.col-sm-3 > .badge').should(($btn3) => {
           expect($btn3.text()).not.to.eq(BookingNumber)
        }) 

        //delete a booking
        cy.get(':nth-child(1) > .text-right > .btn').click()
        cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer')
          .get('.btn-md').contains('Cancel').click({ force: true })
          cy.get('#cancelCloseBtn').click({ force: true })

        // compare the two texts
        // and make sure they are same
        cy.get(':nth-child(1) > .badge').should(($btn2) => {
          expect($btn2.text()).to.eq(BookingNumber)
       })
       
       cy.get(':nth-child(1) > :nth-child(1) > :nth-child(1) > .panel > .panel-heading > .row > .col-sm-3').click()
       .get('.col-sm-3 > .badge').should(($btn3) => {
        expect($btn3.text()).to.eq(BookingNumber)
     })
   })
  
  })
  
 
  it('Book first available time slots with new Bank Details', function() {
    
    cy.get(':nth-child(1) > .panel > .panel-footer > [href="/book/index"]').click({force: true})
          
    //choose indoors, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#showDate')
       .invoke('val', '2020-12-13')
       .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click({force: true});
    cy.get('.media-heading > a').should('contain','Lundby-Biskopsgården Klubb');
      
      cy.wait(100)
      //choose time and slots          
      cy.get('.no-margin.no-padding').first().click()  
      cy.get('[class="btn btn-success btn-sm"]').first().focus().click()

      cy.get('#username').type('abc002@gmail.com')
      cy.get("#password").type('1234567')
      cy.get('#loginForm > .checkbox > label').click()      
      cy.get('#loginForm > .btn').click({ force: true })  

        cy.get(':nth-child(2) > .radio > label').click({force: true})
        cy.get('#btnSubmit').click() // submit payment
        cy.get('#adyen-encrypted-form')
                .find('[data-encrypted-name="number"]').type('4111111111111111')
                cy.get('#adyen-encrypted-form').find('[data-encrypted-name="holderName"]').type('Vivi')
                cy.get('#adyen-encrypted-form').find('[data-encrypted-name="expiryMonth"]').select('03')
                cy.get('#adyen-encrypted-form').find('[data-encrypted-name="expiryYear"]').select('2030')
                cy.get('#adyen-encrypted-form').find('[data-encrypted-name="cvc"]').type('737')
                
        cy.get('#adyen-encrypted-form > .modal-footer > .btn-success').click()
        cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()
       })
  

 it('Booking first available slot using Gift Card/pucch Card', function() {
  //cy.get(':nth-child(1) > .panel > .panel-footer > [href="/book/index"]').click({force: true})
          
  //choose indoors, sport, club's name, then smash
  cy.get('#inOutCourt').select('Indoors',{ force: true })
  cy.get('#showDate')
     .invoke('val', '2020-12-12')
     .trigger('change')
  cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
  cy.get('.col-sm-2 > .btn').eq(0).click({force: true});
  cy.get('.media-heading > a').should('contain','Lundby-Biskopsgården Klubb');
    
    cy.wait(100)
    //choose time and slots          
    cy.get('.no-margin.no-padding').first().click()  
    cy.get('[class="btn btn-success btn-sm"]').first().focus().click()

    cy.get('#username').type('abc002@gmail.com')
    cy.get("#password").type('1234567')
    cy.get('#loginForm > .checkbox > label').click()      
    cy.get('#loginForm > .btn').click({ force: true })  
    
    //  cy.get('.col-sm-8 > :nth-child(3) > .radio > label').click({ force: true}) // payment method´---punch card
      cy.get('.col-sm-8 > :nth-child(4) > .radio > label').click({ force: true}) // payment metho-----gift card
      cy.get('select').first().select
      cy.get('[class="btn btn-md btn-success"]').click() // submit payment
  
      cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking       
                        
  }) 


  
  it('Booking first available slot using Coupen', function() {
    cy.visit('/');
    cy.get('.navbar-left > :nth-child(1) > a > span').click({force: true})// click on booking tab
  
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#showDate')
       .invoke('val', '2020-12-13')
       .trigger('change')
    cy.get('#q').click().type("Kronan Sports Club") 
    cy.get('.col-sm-2 > .btn').eq(0).click({force: true});
    cy.get('.media-heading > a').should('contain','Kronan Sports Club');
      
      cy.wait(100)
      //choose time and slots          
      cy.get('.no-margin.no-padding').first().click()  
      cy.get('[class="btn btn-success btn-sm"]').first().focus().click()
  
      cy.get('#username').type('abc002@gmail.com')
      cy.get("#password").type('1234567')
      cy.get('#loginForm > .checkbox > label').click()      
      cy.get('#loginForm > .btn').click({ force: true })  
 
    cy.get('.col-sm-8 > :nth-child(1) > .radio > label').click({ force: true})
    cy.get(':nth-child(11) > .row > .toggle-collapse-chevron > .right-margin10').click()
    cy.get('#promoCode').type('blackfriday2020')
    cy.get('#applyPromoCode').click()
    cy.get('#btnSubmit').click() // submit payment
    cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking                     
  })

  

})
