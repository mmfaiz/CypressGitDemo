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


  
    it('Book Multipe Available Time Slots using Gift Card', function() {       
      

    cy.get('.btn-group > .btn').click().get('.selected > a').click() 
      
          
      cy.get('#date').click().clear().type('2020-12-12')  // Select Date from Date Picker
        

        cy.get('#q').click().type("Kronan Sports Club")                                                      // search for club
        cy.get('button.btn-block.btn-success').click()                                                      // click smash         
        
        cy.get('#slots_667').contains('16').click({ force: true})                                               //Select specific time
      
        
        cy.get('#667_1607785200000 > .list-group > :nth-child(2) > table > tbody > tr > [width="45%"]').should('contain', 'Xu Xin') 
        
        cy.get('#se4ea4f2075bc03f00175bc04d0950ca3').click({ force: true})                                     //Select specific Time slot

        cy.get('#trailingSlots_e4ea4f2075bc03f00175bc04d0950ca3').select('19:00')
       
        cy.get('div.radio-success').contains('Gift card').click({ force: true})                                // Select payment method
      
        cy.get('#customerGiftCardId').select('5571').should('have.value', '5571')                             // Select specific GiftCard for payment        
        
        cy.get('#btnSubmit').should('have.value','Book').click()                                             // submit payment
       
        cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()
    })


    })


    