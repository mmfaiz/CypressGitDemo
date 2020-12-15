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
        
        
        //cy.get(':nth-child(1) > .list-favorites > .favorite-item > .media > .media-body > .media-heading > a').click()
        cy.get('div.panel-default').first().contains('Kronan Sports Club').click()     

        cy.get('#sportTabs').contains('Badminton').click() 
       cy.get('#picker_daily').click()
       cy.get('div.datepicker-days').contains('27').click()

        cy.get('td.slot.free').contains('e4ea4f2075bc03f00175bc04d1e00f6f').click()

          
       
        cy.get('div.radio-success').contains('Gift card').click({ force: true})                                // Select payment method
      
        cy.get('#customerGiftCardId').select('5571').should('have.value', '5571')                             // Select specific GiftCard for payment        
        
        cy.get('#btnSubmit').should('have.value','Book').click()                                             // submit payment
       
        cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click()
    })


    })


    