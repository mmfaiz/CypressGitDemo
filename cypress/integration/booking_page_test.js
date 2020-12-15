// Successful login 


describe('Test different options to Book a time slot from Booking Page', function() {

      
    it('Login Successful', function() {
  
          //Visit the Matchi AB  Website
          cy.visit('/');
          cy.get('a').then(link => {cy.request(link.prop('href')).its('status').should('eq', 200);});
             

          // Navigating to "login"
          

        cy.get('.navbar-right > :nth-child(2) > a > span').click()   
        cy.get('#username').type('abc0001@gmail.com')
        cy.get("#password").type('12345678')
        cy.get('#loginForm > .checkbox > label').click()      
        cy.get('#loginForm > .btn').click()
       
               
       
      })/*
      
  
    it('Book Multipe Available Time Slots using Gift Card', function() {
  
        
       // cy.get('ul.navbar-left').contains('Book').click()                                                       // click on booking tab   
        
       // cy.get('.navbar-right > :nth-child(1) > .dropdown-toggle > span').click()        // Change language
          //.get('ul.dropdown-menu').contains('ENGLISH').click({force: true})


    //

    cy.get('.btn-group > .btn').click()
      .get('.selected > a').click()
      //     .get('[data-original-index="2"] > a').click()
     // cy.get('#date').click()
     // cy.get('tbody > :nth-child(2) > :nth-child(7)').click()
      
          
      cy.get('#date').click().clear().type('2020-12-12') 
        //cy.get('#showDate').click()                                                                             // Select Date from Date Picker
        //cy.contains('12').click()

        cy.get('#q').click().type("Kronan Sports Club")                                                          // search for club
        cy.get('button.btn-block.btn-success').click()                                                      // click smash         
        
        cy.get('#slots_667').contains('16').click({ force: true})                                               //Select specific time
      //  cy.get('#667_1607785200000').should('contain', 'Xu Xin')                                               // Confirm the playing Court Name
        
        cy.get('#667_1607785200000 > .list-group > :nth-child(2) > table > tbody > tr > [width="45%"]').should('contain', 'Xu Xin') 
        
        cy.get('#se4ea4f2075bc03f00175bc04d0950ca3').click({ force: true})                                     //Select specific Time slot

        cy.get('#trailingSlots_e4ea4f2075bc03f00175bc04d0950ca3').select('19:00')
       
        cy.get('div.radio-success').contains('Gift card').click({ force: true})                                // Select payment method
      
        cy.get('#customerGiftCardId').select('5571').should('have.value', '5571')                             // Select specific GiftCard for payment        
        
        cy.get('#btnSubmit').should('have.value','Book').click()                                                           // submit payment
       // cy.get('#btnSubmit')
        //cy.get('a.btn.btn-md.btn-success').click({ force: true})                                      // finish booking
        cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()
    })
*/

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
        
        cy.get('li').first().click({force: true})
        cy.get('.slot.free').first().click({force: true})

       // cy.get('#slots_667').contains('18').click({ force: true})
       // cy.get('#667_1607533200000 > .list-group > :nth-child(3) > table > tbody > tr > [width="45%"]').should('contain', 'Ma Long')
        
        //cy.get('#se4ea4f2075bc03f00175bc04cdba067f').click({force: true}) 
       
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