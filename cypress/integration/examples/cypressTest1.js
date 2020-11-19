// type definitions for Cypress object "cy"
/// <reference types="cypress" />
 
describe('My First Cypress Test', function() {
    it('Visits the ToolsQA Demo Page and check the menu items', function() {
    //Visit the Demo QA Website
    cy.visit("https://demoqa.com/");
    
   // Clicking on Widget Menu Item
    cy.get(':nth-child(4) > :nth-child(1) > .avatar > svg').click();
  
    //Verify number of items present under the Widget Tab
    cy.get(':nth-child(4) > .element-list > .menu-list > li').should('have.length',9);
  
  })
  })