describe('Registration test', () => {
    it('It should register a user', () => {

        //Visit the Matchi AB  Website
      cy.visit('/');
       
    // Navigating to "Registration"
      cy.get('.navbar-right > :nth-child(3) > a > span').click();
      
      //cy.get('.col-md-offset-2 > .btn').click();

      
        cy.get('#email').type('xyz0001@gmail.com')
        cy.get("#firstname").type('xyz0001')
        cy.get('#lastname').type('xyz')
        cy.get('#password').type('12345678')
        cy.get('#password2').type('12345678')  
       // cy.get('#recaptcha-anchor').click()
        cy.get('#saveButton').click();
        cy.get('div.checkbox > label').first().click()
        cy.get('#acceptConsentModalSubmit').click();
       
      
    })

    
  })