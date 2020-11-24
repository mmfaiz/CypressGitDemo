describe('Loggin test', () => {
    it('Loggin sida', () => {
      cy.visit('https://test.matchiplay.app/login/auth?returnUrl=%2Fprofile%2Fhome')
      cy.get('input[name="j_username"]').eq(0).type('abc001@gmail.com')
      cy.get('input[name="j_password"]').eq(0).type('1234567')
      cy.wait(1000)
      cy.get('#loginForm > .btn').eq(0).click()
    })
  })