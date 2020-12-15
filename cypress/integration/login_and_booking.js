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
      cy.get('#loginForm > .btn').click()    
    })

    it('Book First Available Time Slots using Saved Credit Card', function() {

      
        
  
      cy.get('ul.navbar-left').contains('Book').click()                                                       // click on booking tab   
      cy.get('.navbar-right > :nth-child(1) > .dropdown-toggle > span').click()        // Change language
        .get('ul.dropdown-menu').contains('ENGLISH').click({force: true}) 
  
      cy.get('button.btn.dropdown-toggle.selectpicker.form-control').contains('Indoor/Outdoor').click()        // select indoor game
        .get('div.dropdown-menu.open').contains('Indoors').click()     
      
      cy.get('button.btn.dropdown-toggle.selectpicker.form-control').contains('Select sport').click()         // select sport Name
        .get('div.dropdown-menu.open').contains('Table tennis').click()
        
        
      cy.get('#showDate').click().clear().type('2020-12-09')                                  // Select Date from Date Picker
        //.get('div.datepicker-days').contains('8').click()

      cy.get('#q').click().type("Kronan Sports Club")                                                          // search for club
      cy.get(':nth-child(5) > .col-sm-2').click()
      // cy.get('button.btn.btn-success.col-xs-12').click()                                                      // click smash         
      
      //cy.get('li').first().click({force: true})
      //cy.get('.slot.free').first().click({force: true})

      cy.get('#slots_667').contains('18').click({ force: true})
      cy.get('#667_1607533200000 > .list-group > :nth-child(3) > table > tbody > tr > [width="45%"]').should('contain', 'Ma Long')
      
      cy.get('#se4ea4f2075bc03f00175bc04cdba067f').click({force: true}) 
     
      cy.get('#CREDIT_CARD_RECUR').click({force: true})                                            // Select payment method
    
      
      cy.get('#btnSubmit').should('have.value','Finish payment').click()                            // submit payment
                                       
      cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()   // finish booking
  
  })


  it('Book First Available Time Slots with New Bank Details', function() {

    

      cy.get('ul.navbar-left').contains('Book').click()                                                       // click on booking tab
      
      cy.get('.navbar-right > :nth-child(1) > .dropdown-toggle > span').click()        // Change language
      .get('ul.dropdown-menu').contains('ENGLISH').click({force: true}) 
  
      cy.get('button.btn.dropdown-toggle.selectpicker.form-control').contains('Indoor/Outdoor').click()        // select indoor game
        .get('div.dropdown-menu.open').contains('Indoors').click()    
      
      cy.get('button.btn.dropdown-toggle.selectpicker.form-control').contains('Select sport').click()         // select sport Name
        .get('div.dropdown-menu.open').contains('Badminton').click()
        
        
        cy.get('#showDate').click().clear().type('2020-12-10')                                // Select Date from Date Picker
      
      
      
      cy.get('#q').click().type("Kronan Sports Club")                                                          // search for club
      cy.get('button.btn.btn-success.col-xs-12').click()                                                      // click smash 
      
      cy.get('#slots_667').contains('17').click({ force: true})
      cy.get('#667_1607616000000 > .list-group > :nth-child(3) > table > tbody > tr > [width="45%"]').should('contain','Lee Chong Wei')
      
      cy.get('#se4ea4f2075bc03f00175bc04ceb80874').click({force: true}) 
      
      
     // cy.get('li').first().click({force: true})                                                            // select first available time slot
     // cy.get('.slot.free').first().click({force: true})

     
     
      cy.get('#CREDIT_CARD').should('have.value', 'CREDIT_CARD').click({force: true})                      // Select payment method
         
     
  
      
      cy.get('#btnSubmit').click()                                                                            // submit payment
      cy.get('#adyen-encrypted-form')                                                                         // fill new Bank Details
        .find('[data-encrypted-name="number"]').type('4111111111111111')
      cy.get('#adyen-encrypted-form').find('[data-encrypted-name="holderName"]').type('Faiz')
      cy.get('#adyen-encrypted-form').find('[data-encrypted-name="expiryMonth"]').select('03')
      cy.get('#adyen-encrypted-form').find('[data-encrypted-name="expiryYear"]').select('2030')
      cy.get('#adyen-encrypted-form').find('[data-encrypted-name="cvc"]').type('737')
          
      cy.get('#adyen-encrypted-form > .modal-footer > .btn-success').click()                                  // Finish Payment
      cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()             // close windows
          
  })

  it('Book First Available Time Slots using Promo Code', function() {

       cy.get('ul.navbar-left').contains('Book').click()                                                       // click on booking tab   
       
       cy.get('.navbar-right > :nth-child(1) > .dropdown-toggle > span').click()       // Change language
      .get('ul.dropdown-menu').contains('ENGLISH').click({force: true})

       cy.get('button.btn.dropdown-toggle.selectpicker.form-control').contains('Indoor/Outdoor').click()        // select indoor game
         .get('div.dropdown-menu.open').contains('Indoors').click()    
  
       cy.get('button.btn.dropdown-toggle.selectpicker.form-control').contains('Select sport').click()         // select sport Name
         .get('div.dropdown-menu.open').contains('Table tennis').click()
    
<<<<<<< Updated upstream
    it('Book first available time slots using Saved Card', function() {  
      cy.get('.col-sm-4 > :nth-child(4) > .btn').click() // click on booking tab
      // select indoor game
=======
         cy.get('#showDate').click().clear().type('2020-12-11')   // Select Date from Date Picker
                                                                                 
         

       cy.get('#q').click().type("Kronan Sports Club")                                                          // search for club
       cy.get('button.btn.btn-success.col-xs-12').click()                                                      // click smash     
       
       
       cy.get('#slots_667').contains('16').click({ force: true})
      
      cy.get('#667_1607698800000 > .list-group > :nth-child(4) > table > tbody > tr > [width="45%"]').should('contain','Wang Liqin')
      
      cy.get('#se4ea4f2075bc03f00175bc04d02a0b95').click({force: true})
      
  
      // cy.get('li').first().click({force: true})                                                           // select firt available time slot
       //cy.get('.slot.free').first().click({force: true})   
 
       cy.get('#CREDIT_CARD_RECUR').click({force: true})                                                       // Select payment method    
  
       cy.get('div.toggle-collapse-chevron').contains('Add Promo Code').click()                           // click on PromoCode Drop down menue  
       cy.get('#promoCode').type('k-2020')                                                               // select and type Promo Code
       cy.get('#applyPromoCode').click()                                                                // Apply promocode  
  
       cy.get('#btnSubmit').should('have.value','Finish payment').click()                              // submit payment                                     
       cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()       // finish booking

})








     })
    
    /*

    it('Book first available time slots using Saved Card', function() {

    
      cy.get('.navbar-left > :nth-child(2) > a > span').click({force: true})// click on booking tab
  
     // cy.get('.col-sm-4 > :nth-child(4) > .btn').click() // click on booking tab
  // select indoor game
>>>>>>> Stashed changes
      cy.get(':nth-child(1) > .form-group > .btn-group > .btn > .filter-option').click({force: true})
      .get(':nth-child(1) > .form-group > .btn-group > .open > .dropdown-menu > [data-original-index="1"] > a > .text').click({force: true})
      //cy.get('#inOutCourt').select('Indoors',{ force: true }) // select in or out doors courts 
  
      // select sport
      cy.get(':nth-child(4) > :nth-child(2) > .form-group > .btn-group > .btn > .filter-option').click({force: true})
        .get(':nth-child(2) > .form-group > .btn-group > .open > .dropdown-menu > [data-original-index="4"] > a').click({force: true})
      //cy.get('#sport').select('Table tennis',{ force: true }) // select sports type     
      cy.get('#q').click().type("Kronan Sports Club") // search for club
      cy.get('.col-sm-2 > .btn').click() // click smash      
      
      cy.get('li').first().click({force: true})
      cy.get('.slot.free').first().click({force: true}  )
     
      cy.get('.payment-method > .radio > label')
      //cy.get('select').eq(1).select('e4ea4f2075bc03f00175bc04d1500e65').should('have.value', 'e4ea4f2075bc03f00175bc04d1500e65')
     //cy.get('.col-sm-8 > :nth-child(1) > .radio > label').click({ force: true}) // payment method
      cy.get('#btnSubmit').click() // submit payment
      cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking
  })
  
  /* 
  it('Book first available time slots using Gift Card', function() {
     cy.get('.navbar-left > :nth-child(2) > a > span').click({force: true})// click on booking tab
  // cy.get('.col-sm-4 > :nth-child(4) > .btn').click() // click on booking tab
  // select indoor game
      cy.get(':nth-child(1) > .form-group > .btn-group > .btn > .filter-option').click({force: true})
        .get(':nth-child(1) > .form-group > .btn-group > .open > .dropdown-menu > [data-original-index="1"] > a > .text').click({force: true})
      //cy.get('#inOutCourt').select('Indoors',{ force: true }) // select in or out doors courts 
  
      // select sport
      cy.get(':nth-child(4) > :nth-child(2) > .form-group > .btn-group > .btn > .filter-option').click({force: true})
        .get(':nth-child(2) > .form-group > .btn-group > .open > .dropdown-menu > [data-original-index="4"] > a').click({force: true})
      //cy.get('#sport').select('Table tennis',{ force: true }) // select sports type     
      cy.get('#q').click().type("Kronan Sports Club") // search for club
      cy.get('.col-sm-2 > .btn').click() // click smash      
      
      cy.get('li').first().click({force: true})
      cy.get('.slot.free').first().click({force: true}  )  
   
    //cy.get('.payment-method > .radio > label')
    cy.get('.col-sm-8 > :nth-child(4) > .radio > label').click({ force: true}) // payment with gift card
    //cy.get('.col-sm-8 > :nth-child(3) > .radio > label').click({ force: true}) // payment with punch card
    cy.get('select').first().select
       
     cy.get('#btnSubmit').click() // submit payment
    cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking
  })

 it('Booking first available slot with new Bank Details', function() {
    cy.get('.navbar-left > :nth-child(2) > a > span').click({force: true})// click on booking tab
  
     // cy.get('.col-sm-4 > :nth-child(4) > .btn').click() // click on booking tab
  // select indoor game
      cy.get(':nth-child(1) > .form-group > .btn-group > .btn > .filter-option').click({force: true})
      .get(':nth-child(1) > .form-group > .btn-group > .open > .dropdown-menu > [data-original-index="1"] > a > .text').click({force: true})
      //cy.get('#inOutCourt').select('Indoors',{ force: true }) // select in or out doors courts 
  
      // select sport
      cy.get(':nth-child(4) > :nth-child(2) > .form-group > .btn-group > .btn > .filter-option').click({force: true})
        .get(':nth-child(2) > .form-group > .btn-group > .open > .dropdown-menu > [data-original-index="4"] > a').click({force: true})
      //cy.get('#sport').select('Table tennis',{ force: true }) // select sports type     
      cy.get('#q').click().type("Kronan Sports Club") // search for club
      cy.get('.col-sm-2 > .btn').click() // click smash      
      
      cy.get('li').first().click({force: true})
      cy.get('.slot.free').first().click({force: true}  )
    
 
    cy.get(':nth-child(2) > .radio > label').click({force: true})
    cy.get('#btnSubmit').click() // submit payment
    cy.get('#adyen-encrypted-form')
            .find('[data-encrypted-name="number"]').type('4111111111111111')
            cy.get('#adyen-encrypted-form').find('[data-encrypted-name="holderName"]').type('Faiz')
            cy.get('#adyen-encrypted-form').find('[data-encrypted-name="expiryMonth"]').select('03')
            cy.get('#adyen-encrypted-form').find('[data-encrypted-name="expiryYear"]').select('2030')
            cy.get('#adyen-encrypted-form').find('[data-encrypted-name="cvc"]').type('737')
            
            cy.get('#adyen-encrypted-form > .modal-footer > .btn-success').click()
            cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()
            
    
                        
  }) 

  
  
  it('Booking first available slot using Coupen', function() {
    cy.get('.navbar-left > :nth-child(2) > a > span').click({force: true})// click on booking tab
  
     // cy.get('.col-sm-4 > :nth-child(4) > .btn').click() // click on booking tab
  // select indoor game
      cy.get(':nth-child(1) > .form-group > .btn-group > .btn > .filter-option').click({force: true})
      .get(':nth-child(1) > .form-group > .btn-group > .open > .dropdown-menu > [data-original-index="1"] > a > .text').click({force: true})
      //cy.get('#inOutCourt').select('Indoors',{ force: true }) // select in or out doors courts 
  
      // select sport
      cy.get(':nth-child(4) > :nth-child(2) > .form-group > .btn-group > .btn > .filter-option').click({force: true})
        .get(':nth-child(2) > .form-group > .btn-group > .open > .dropdown-menu > [data-original-index="4"] > a').click({force: true})
      //cy.get('#sport').select('Table tennis',{ force: true }) // select sports type     
      cy.get('#q').click().type("Kronan Sports Club") // search for club
      cy.get('.col-sm-2 > .btn').click() // click smash      
      
      cy.get('li').first().click({force: true})
      cy.get('.slot.free').first().click({force: true}  )
    
 
    cy.get(':nth-child(1) > .radio > label').click({force: true})
    cy.get(':nth-child(9) > .row > .toggle-collapse-chevron > .right-margin10').click()
   cy.get('#promoCode').type('blackfriday2020')
   cy.get('#applyPromoCode').click()
    cy.get('#btnSubmit').click() // submit payment
    cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking                     
  })
  
 it('Show all my booking and cancel first one', functizzzon() {    
    cy.get(':nth-child(3) > .dropdown-toggle > :nth-child(1)').click()
<<<<<<< Updated upstream
      .get('a[href*="bookings"]').contains('Show all my bookings').click({ force: true }) // SHow all bookings
    
      cy.get(':nth-child(1) > .text-right > .btn').click({force:true})
      cy.get('.btn-rnd.btn-danger').click()
      //cy.get('#cancelCloseBtn').click()  
  })
  */

it('Go to Profile setting and change it', function() {
  
      cy.get('.navbar-brand').contains('matchi',{ matchCase: false }) // check Matchi text button
=======
.get('//a[contains(text(),'Show all my bookings')]').click()

    .get('a[href*="bookings"]').contains('Show all my bookings').click({ force: true }) // SHow all bookings
   
    
       
    
    
})
it('Go to Profile setting and change it', function() {
  
cy.get('.navbar-brand').contains('matchi',{ matchCase: false }) // check Matchi text button

cy.get(':nth-child(3) > .dropdown-toggle > :nth-child(1)').click()
.get('//a[contains(text(Show all my bookings)]').click()

>>>>>>> Stashed changes

      cy.get('.navbar-right > .user-menu > .dropdown-toggle > span').click()
              //.get('.navbar-right > .usermenu > .dropdown-menu > :nth-child(2) > a').click()
      cy.get('[class="dropdown-menu"]')
        .contains('My profile')
        .focus()
        .click({force: true})

      cy.get('.col-sm-5 > .btn').click()
      cy.get('#firstname').clear().type('Alexander')
      cy.get('#lastname').clear().type('Andersson')
      cy.get(':nth-child(2) > #save').click()

      // Check if the following buttons  working are not 
<<<<<<< Updated upstream
      cy.get('a[href="https://svenskpadel.se/"]').should('have.attr', 'target', '_blank').click()
      cy.get('a[href="https://www.tennis.se/"]').should('have.attr', 'target', '_blank').click()
      //cy.get('a[href="https://www.tennismagasinet.se/"]').should('have.attr', 'target', '_blank').click()  //den funkar inte.
      //cy.get('a[href="https://jobs.matchi.se/"]').should('have.attr', 'target', '_blank').click()
      cy.get(':nth-child(3) > .partner > .center-text > .img-responsive').click()
      cy.get(':nth-child(4) > .partner > .center-text > .img-responsive').click()
=======
     // cy.get('a[href="https://svenskpadel.se/"]').should('have.attr', 'target', '_blank').click()
     // cy.get('a[href="https://www.tennis.se/"]').should('have.attr', 'target', '_blank').click()
      //cy.get('a[href="https://www.tennismagasinet.se/"]').should('have.attr', 'target', '_blank').click()
      //cy.get('a[href="https://jobs.matchi.se/"]').should('have.attr', 'target', '_blank').click()
     // cy.get(':nth-child(3) > .partner > .center-text > .img-responsive').click()
     // cy.get(':nth-child(4) > .partner > .center-text > .img-responsive').click()


})



>>>>>>> Stashed changes
})
*/