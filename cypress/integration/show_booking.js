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
        
    
    
    })
  
    it('Show all my current and past booking ', function() {
  
  // show all my current Bookings
      cy.get('.dropdown-toggle').eq(3).click({force:true})
        .get('ul.dropdown-menu.dropdown-bookings').contains('Show all my bookings')
        .click({force:true})     

        // test Badge information about booking in Show all my booking  webpage
           cy.get('.badge-info').then($nbookings => {
           const nbookings = parseFloat($nbookings.text())
           cy.get('tr > .text-right').its('length').should('eq', nbookings)
        })
      // cy.wait(5000)
        // Show past bookings
        cy.get('div.col-sm-12').children().contains(' Show past bookings').click()
      // cy.wait(5000)
        cy.go('back')        
   

    //delete a booking
            cy.get(':nth-child(1) > .text-right > .btn').click()
            cy.scrollTo('bottom')
            cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer')
              .get('.btn-md').contains('Cancel').click({ force: true })
              cy.get('#cancelCloseBtn').click({ force: true })  
    
  
  
    // show all my Recording
    
    cy.get('.dropdown-toggle').eq(3).click({force:true})
    .get('ul.dropdown-menu.dropdown-bookings').contains('Show all my recordings')
    .click({force:true})     
             
    })
  
  
  })

  