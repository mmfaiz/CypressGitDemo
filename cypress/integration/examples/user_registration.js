describe('Loggin test', () => {
    it('Loggin sida', () => {
        //Visit the Matchi AB  Website
        cy.visit("https://test.matchiplay.app/");
       
    // Navigating to "Registration"
    cy.get('.navbar-right > :nth-child(3) > a > span').eq(0).click();
    cy.wait(1000);

      cy.get('.col-md-offset-2 > .btn').eq(0).click();

      cy.get('#email').eq(0).type('abc@gmail.com')
      cy.get("#firstname").eq(0).type('abc001')
      cy.get('#lastname').eq(0).type('abc002')
      cy.get('#password').eq(0).type('12345678')
      cy.get('#password2').eq(0).type('12345678')

      cy.get('[style="width: 304px; height: 78px;"] > div > iframe').eq(0).click();
      cy.wait(1000)
      cy.get('#saveButton').eq(0).click();
     
      
      cy.get('#loginForm > .btn').eq(0).click();
     
      cy.get(':nth-child(15) > label').eq(0).click();
      cy.get(':nth-child(16) > label').eq(0).click();
      cy.get(':nth-child(17) > label').eq(0).click();
      cy.get('.modal-footer').eq(0).click();
      
    })
  })