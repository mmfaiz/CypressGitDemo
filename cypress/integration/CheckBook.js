describe('Loggin and book', () => {
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
    
    //go to book page
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
          
    //choose indoors, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#showDate')
       .invoke('val', '2020-12-9')
       .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
    cy.get('.media-heading > a').should('contain','Lundby-Biskopsgården Klubb');
      
      cy.wait(100)
      //choose time and slots          
      cy.get('.no-margin.no-padding').first().click()  
      cy.get('.slot.free').first().click()
      cy.get('[class="trailingSlotSelector"]')
            .select('14:00')
      cy.get('[class="btn btn-md btn-success"]').click() // submit payment

     cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking
    })
 })

