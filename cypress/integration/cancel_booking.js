// Successful login 


describe('Test different options to Book a time slot from Booking Page', function() {

      
    it('Cancel booking from Dropdown menu ', function() {
  
          //Visit the Matchi AB  Website
          cy.visit('/');
          cy.get('a').then(link => {cy.request(link.prop('href')).its('status').should('eq', 200);});             

          // Navigating to "login"          

        cy.get('.navbar-right > :nth-child(2) > a > span').click()   
        cy.get('#username').type('abc0001@gmail.com')
        cy.get("#password").type('12345678')
        cy.get('#loginForm > .checkbox > label').click()      
        cy.get('#loginForm > .btn').click()



        // test Badge information about booking in main webpage
      cy.get('.badge').then($nbookings => {
      const nbookings = parseFloat($nbookings.text())
      cy.get('a.userCancelBooking').its('length').should('eq', nbookings)
      })
           
      cy.get('.dropdown-toggle').eq(3).click()
      .get('a.userCancelBooking').first() 
      .click() 

      cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn-md')

      cy.get('a.btn.btn-md.btn-danger').first().click()
      cy.get('#cancelCloseBtn').click()

       
      })


      
})


    