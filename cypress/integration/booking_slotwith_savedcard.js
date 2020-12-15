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
       
               
       
      })
      
  

    it('Book selected Time Slots using Saved Credit Card', function() {

      
        
  
        cy.get('ul.navbar-left').contains('Book').click()                                                       // click on booking tab   
        cy.get( '.navbar-right > :nth-child(1) > .dropdown-toggle > span').click()        // Change language
          .get('ul.dropdown-menu').contains('ENGLISH').click({force: true}) 
    
        cy.get('button.btn.dropdown-toggle.selectpicker.form-control').contains('Indoor/Outdoor').click()        // select indoor game
          .get('div.dropdown-menu.open').contains('Indoors').click()     
        
        cy.get('button.btn.dropdown-toggle.selectpicker.form-control').contains('Select sport').click()         // select sport Name
          .get('div.dropdown-menu.open').contains('Table tennis').click()
          
          
        cy.get('#showDate').click().clear().type('2020-12-22')                                  // Select Date from Date Picker
          //.get('div.datepicker-days').contains('8').click()

        cy.get('#q').click().type("Kronan Sports Club")                                                          // search for club
        cy.get(':nth-child(5) > .col-sm-2').click()
        // cy.get('button.btn.btn-success.col-xs-12').click()                                                      // click smash         
        
       // cy.get('li').first().click({force: true})
       // cy.get('.slot.free').first().click({force: true})

        cy.get('#slots_667').contains('18').click({ force: true})
        cy.get('#667_1608656400000 > .list-group > :nth-child(3) > table > tbody > tr > [width="45%"]').should('contain', 'Ma Long')
        cy.get('#se4ea4f2075bc03f00175bc04ce3507ba').click({force: true}) 
             
       
        cy.get('#CREDIT_CARD_RECUR').click({force: true})                                            // Select payment method
      
        
        cy.get('#btnSubmit').should('have.value','Finish payment').click()                            // submit payment
                                         
        cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()   // finish booking
    
    })







       })