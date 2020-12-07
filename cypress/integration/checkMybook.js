describe('Loggin and show user all books', () => {
    it('Loggin sida', () => {
      cy.visit('https://test.matchiplay.app/login/auth?returnUrl=%2Fprofile%2Fhome')  
      //loggin, input username, password, test username on the page.
      cy.get('input[name="j_username"]').eq(0).type('abc001@gmail.com')
      cy.get('input[name="j_password"]').eq(0).type('1234567').should('have.value','1234567')
      cy.wait(500)
      cy.get('#loginForm > .btn').eq(0).click()
      cy.get('.col-sm-4 > .media > .media-body > .media-heading > a').should('contain','Anna')
      
      cy.get('#acceptConsentModalCheckBoxTerms').click({force: true}) 
      cy.get('#acceptConsentModal > .modal-dialog > .modal-content > .modal-footer').contains('Accept').click({force: true} )
       
      cy.get(':nth-child(3) > .dropdown-toggle > span').click()
        
       cy.get('.navbar-right > .open > .dropdown-menu > :nth-child(1) > a').then(elem => {
        // elem is a jQuery object
        console.log(elem.text());
        if (elem.text() == 'You have no upcoming bookings') {
            // do something
            cy.get('.navbar-right > .open > .dropdown-menu > :nth-child(3) > a').click({force:true})
            cy.get('.page-header').should('contain','My reservation')
            cy.get(':nth-child(1) > :nth-child(1) > :nth-child(1) > .panel > .panel-body > .text-muted').should('contain','You have no upcoming bookings')    
        }
        else {
            // ..
            cy.get('.navbar-right > .open > .dropdown-menu').contains('Show all my bookings').click()
            cy.get('.col-sm-3 > .badge').should('be.visible')

            cy.get('#acceptConsentModal > .modal-dialog > .modal-content > .modal-footer').contains('Accept').click({force: true} )
            cy.get('#acceptConsentModalCheckBoxTerms').click({force: true})

           //delete a booking
           cy.get(':nth-child(1) > .text-right > .btn').click()
           cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer')
             .get('.btn-md').contains('Cancel').click({ force: true })
           cy.get('#cancelCloseBtn').click()
           
        }
      })
  })
  
})


