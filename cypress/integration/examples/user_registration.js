describe('Registration test', () => {
    it('It should register a user', () => {

        //Visit the Matchi AB  Website
      cy.visit('/');
       
    // Navigating to "Registration"
      cy.get('.navbar-right > :nth-child(3) > a > span').eq(0).click();
      
      cy.get('.col-md-offset-2 > .btn').eq(0).click();

      cy.get('#email').type('abcde@gmail.com')
      cy.get("#firstname").type('abcd001')
      cy.get('#lastname').type('abc002')
      cy.get('#password').type('12345678')
      cy.get('#password2').type('12345678')

      
      cy.get('iframe')
            .first()
            .its('0.contentDocument.body')
            .should('not.be.undefined')
            .and('not.be.empty')
            .then(cy.wrap)
            .find('#recaptcha-anchor')
            .should('be.visible')
            .click();
            //cy.get('[style="width: 304px; height: 78px;"] > div > iframe').eq(0).click();
      
      
     
      cy.get('#saveButton').eq(0).click();
      //cy.get('#loginForm > .btn').eq(0).click();
     
     // cy.get(':nth-child(15) > label').eq(0).click();
     // cy.get(':nth-child(16) > label').eq(0).click();
      //cy.get(':nth-child(17) > label').eq(0).click();
      cy.get('#acceptConsentModalSubmit').eq(0).click({force: true});
      
    })
  })