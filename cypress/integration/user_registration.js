describe('Registration test', () => {
    it('It should register a user', () => {

        //Visit the Matchi AB  Website
      cy.visit('/');
       
    // Navigating to "Registration"
      cy.get('.navbar-right > :nth-child(3) > a > span').click();
      
      //cy.get('.col-md-offset-2 > .btn').click();

      cy.get('#email').type('abcd@gmail.com')
      cy.get("#firstname").type('abcd001')
      cy.get('#lastname').type('abc002')
      cy.get('#password').type('12345678')
      cy.get('#password2').type('12345678')

      cy.get('[style="width: 304px; height: 78px;"] > div > iframe').click()

     
     
      cy.get('#saveButton').click();
     // cy.get('#loginForm > .btn').eq(0).click();
     
      cy.get(':nth-child(15) > label').click();
      cy.get(':nth-child(16) > label').click();
      cy.get(':nth-child(17) > label').click();
      cy.get('#acceptConsentModalSubmit').click({force: true});
      
    })

    Cypress.Commands.add("clickRecaptcha", () => {

      cy.wait(500)
      cy.window().then(win => {
        win.document
          .querySelector("iframe[src*='recaptcha']")
          .contentDocument.getElementById("recaptcha-token")
          .click();
          //cy.get('[style="width: 304px; height: 78px;"] > div > iframe').eq(0).click();
        })
      })
    
  })