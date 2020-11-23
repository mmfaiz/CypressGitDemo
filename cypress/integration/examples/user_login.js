describe('Loggin test', () => {
    it('Loggin sida', () => {
        //Visit the Matchi AB  Website
        cy.visit("https://test.matchiplay.app/");
        //cy.visit('https://test.matchiplay.app/login/auth?returnUrl=%2Fprofile%2Fhome')
     // Navigating to "login"
    cy.get('.navbar-right > :nth-child(2) > a > span').eq(0).click();
    cy.wait(1000);

      cy.get('.col-md-offset-2 > .btn').eq(0).click();

      cy.get('#username').eq(0).type('abc@gmail.com')
      cy.get("#password").eq(0).type('123456')
      cy.get('#loginForm > .checkbox > label').eq(0).click();
      cy.wait(1000)
      cy.get('#loginForm > .btn').eq(0).click()
      // username or password is not correct
    })
  })