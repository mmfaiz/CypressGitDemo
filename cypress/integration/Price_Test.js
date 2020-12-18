describe('Loggin and book', () => {
  it('Test Loggin with membership and check the normal price', () => {
    cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
  //loggin, input username, password, test username on the page.
    cy.get('input[name="j_username"]').eq(0).type('abc001@gmail.com')
    cy.get('input[name="j_password"]').eq(0).type('1234567')
    cy.get('#loginForm > .btn').eq(0).click()
       
      // Book
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
      //choose indoors, datum, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#sport').select('Table tennis',{ force: true })
    cy.get('#showDate')
      .invoke('val', '2020-12-18')
      .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
        
      //check the price of slots  
    cy.get('.no-margin.no-padding').first().click({force: true})  
    cy.get('.slot.free').first().click({force: true})
        
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
       expect(parseFloat($b2price.text())).to.eql(100)
     })
    cy.get('.modal-footer > .btn-default').click()
   })

  it('Test Loggin with membership and check free price', () => {
    cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
  //loggin, input username, password, test username on the page.
    cy.get('input[name="j_username"]').eq(0).type('abc001@gmail.com')
    cy.get('input[name="j_password"]').eq(0).type('1234567')
    cy.get('#loginForm > .btn').eq(0).click()
       
      // Book
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
      //choose indoors, datum, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#sport').select('Table tennis',{ force: true })
    cy.get('#showDate')
      .invoke('val', '2020-12-24')
      .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
        
      //check the price of slots  
    cy.get('.no-margin.no-padding').first().click({force: true})  
    cy.get('.slot.free').first().click({force: true})
        
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
       expect(parseFloat($b2price.text())).to.eql(0)
     })
    cy.get('.modal-footer > .btn-default').click()
 })

 it('Test Loggin with membership and check the weekend price', () => {
  cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
  //loggin, input username, password, test username on the page.
    cy.get('input[name="j_username"]').eq(0).type('abc001@gmail.com')
    cy.get('input[name="j_password"]').eq(0).type('1234567')
    cy.get('#loginForm > .btn').eq(0).click()
       
      // Book
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
      //choose indoors, datum, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#sport').select('Table tennis',{ force: true })
    cy.get('#showDate')
      .invoke('val', '2020-12-19')
      .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
        
      //check the price of slots  
    cy.get('.no-margin.no-padding').first().click({force: true})  
    cy.get('.slot.free').first().click({force: true})
        
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
       expect(parseFloat($b2price.text())).to.eql(120)
     })
    cy.get('.modal-footer > .btn-default').click()
})

it('Test Loggin with membership and check the normal price with coupon', () => {
  cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
  //loggin, input username, password, test username on the page.
    cy.get('input[name="j_username"]').eq(0).type('abc001@gmail.com')
    cy.get('input[name="j_password"]').eq(0).type('1234567')
    cy.get('#loginForm > .btn').eq(0).click()
       
      // Book
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
      //choose indoors, datum, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#sport').select('Table tennis',{ force: true })
    cy.get('#showDate')
      .invoke('val', '2020-12-22')
      .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
        
      //check the price of slots  
    cy.get('.no-margin.no-padding').first().click({force: true})  
    cy.get('.slot.free').first().click({force: true})
        
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
       expect(parseFloat($b2price.text())).to.eql(100)
     })

    cy.get('.col-sm-8 > :nth-child(1) > .radio > label').click({ force: true})
    cy.get(':nth-child(11) > .row > .toggle-collapse-chevron > .right-margin10').click()
    cy.get('#promoCode').type('jul2020')
    cy.get('#applyPromoCode').click()
    
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
      expect(parseFloat($b2price.text())).to.eql(50)
    })    
    cy.get('.modal-footer > .btn-default').click()
  })

  it('Test Loggin without membership and check the normal price', () => {
    cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
  //loggin, input username, password, test username on the page.
    cy.get('input[name="j_username"]').eq(0).type('abc0001@gmail.com')
    cy.get('input[name="j_password"]').eq(0).type('12345678')
    cy.get('#loginForm > .btn').eq(0).click()
       
      // Book
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
      //choose indoors, datum, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#sport').select('Table tennis',{ force: true })
    cy.get('#showDate')
      .invoke('val', '2020-12-18')
      .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
        
      //check the price of slots  
    cy.get('.no-margin.no-padding').first().click({force: true})  
    cy.get('.slot.free').first().click({force: true})
        
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
       expect(parseFloat($b2price.text())).to.eql(150)
     })
    cy.get('.modal-footer > .btn-default').click()
   })

  it('Test Loggin without membership and check low price', () => {
    cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
  //loggin, input username, password, test username on the page.
    cy.get('input[name="j_username"]').eq(0).type('abc0001@gmail.com')
    cy.get('input[name="j_password"]').eq(0).type('12345678')
    cy.get('#loginForm > .btn').eq(0).click()
       
      // Book
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
      //choose indoors, datum, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#sport').select('Table tennis',{ force: true })
    cy.get('#showDate')
      .invoke('val', '2020-12-24')
      .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
        
      //check the price of slots  
    cy.get('.no-margin.no-padding').first().click({force: true})  
    cy.get('.slot.free').first().click({force: true})
        
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
       expect(parseFloat($b2price.text())).to.eql(60)
     })
    cy.get('.modal-footer > .btn-default').click()
 })

 it('Test Loggin without membership and check the weekend price', () => {
  cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
  //loggin, input username, password, test username on the page.
    cy.get('input[name="j_username"]').eq(0).type('abc0001@gmail.com')
    cy.get('input[name="j_password"]').eq(0).type('12345678')
    cy.get('#loginForm > .btn').eq(0).click()
       
      // Book
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
      //choose indoors, datum, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#sport').select('Table tennis',{ force: true })
    cy.get('#showDate')
      .invoke('val', '2020-12-19')
      .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
        
      //check the price of slots  
    cy.get('.no-margin.no-padding').first().click({force: true})  
    cy.get('.slot.free').first().click({force: true})
        
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
       expect(parseFloat($b2price.text())).to.eql(180)
     })
    cy.get('.modal-footer > .btn-default').click()
})

it('Test Loggin without membership and check the normal price with coupon', () => {
  cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
  //loggin, input username, password, test username on the page.
    cy.get('input[name="j_username"]').eq(0).type('abc0001@gmail.com')
    cy.get('input[name="j_password"]').eq(0).type('12345678')
    cy.get('#loginForm > .btn').eq(0).click()
       
      // Book
    cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
      //choose indoors, datum, sport, club's name, then smash
    cy.get('#inOutCourt').select('Indoors',{ force: true })
    cy.get('#sport').select('Table tennis',{ force: true })
    cy.get('#showDate')
      .invoke('val', '2020-12-22')
      .trigger('change')
    cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
    cy.get('.col-sm-2 > .btn').eq(0).click();
        
      //check the price of slots  
    cy.get('.no-margin.no-padding').first().click({force: true})  
    cy.get('.slot.free').first().click({force: true})
        
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
       expect(parseFloat($b2price.text())).to.eql(150)
     })

    cy.get('.col-sm-8 > :nth-child(1) > .radio > label').click({ force: true})
    cy.get(':nth-child(9) > .row > .toggle-collapse-chevron > .right-margin10').click()
    cy.get('#promoCode').type('jul2020')
    cy.get('#applyPromoCode').click()
    
    cy.get('.col-sm-4.col-xs-12 > p > span').last().should(($b2price) => {
      expect(parseFloat($b2price.text())).to.eql(75)
    })    
    cy.get('.modal-footer > .btn-default').click()
})
})


 
