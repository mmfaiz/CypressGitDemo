/// <reference types="cypress" />
 
describe('My First Cypress Test', function() {
    it('Visits the Matchi AB Page and check the menu items', function() {
    //Visit the Matchi AB  Website
    cy.visit("https://test.matchiplay.app/");
    // Navigating to "Booking" 
    cy.get('.navbar-left > :nth-child(1) > a > span').eq(0).click();
    cy.get('.navbar-left > :nth-child(1) > a > span')
   // cy.url().should('include', '/book/index');
   cy.wait(1000);
 // Navigating to "Activities"
    // Navigating to "Activities"
    cy.get('.navbar-left > :nth-child(2) > a > span').eq(0).click();
   // cy.url().should('include', '/activities/index');
   // cy.url().should('include', '/book/index');
     // cy.url().should('include', '/activities/index');
    cy.wait(1000);
// Navigating to "Venues"
    cy.get('.navbar-left > :nth-child(3) > a > span').eq(0).click();
    cy.wait(1000);
// Navigating to "For the Club"
    cy.get('.navbar-left > :nth-child(4) > a > span').eq(0).click();
    cy.wait(1000);
    // Navigating to "Language"
    cy.get(':nth-child(1) > .dropdown-toggle > span').eq(0).click();
    cy.wait(1000);
    // Navigating to "login"
    cy.get('.navbar-right > :nth-child(2) > a > span').eq(0).click();
    cy.wait(1000);
    // Navigating to "Registration"
    cy.get('.navbar-right > :nth-child(3) > a > span').eq(0).click();

    
    

    

   // cy.get('.active > a > span');
   // Clicking on Widget Menu Item
   // cy.get(':nth-child(4) > :nth-child(1) > .avatar > svg').click();
   
    //Verify number of items present under the Widget Tab
   // cy.get(':nth-child(4) > .element-list > .menu-list > li').should('have.length',9);
   
  })
  })