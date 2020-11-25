/// <reference types="Cypress" />

describe('Test of index', function() {
    beforeEach(() => {
          cy.visit('https://test.matchiplay.app')
        })
      it('Check smash!', function() {
        cy.get('#q').eq(0).type('Lundby-Biskopsgården Klubb')
        cy.get("#submit").then($button_start_search=>{
            expect($button_start_search.attr("value")).to.eq("SMASH!");
            cy.get('.col-sm-2 > .hidden-xs').eq(0).click();
        })
      })


    })