

// Successful login 


describe('Matchi AB Automation Testing ', function() {

  before(function()
  {
    cy.fixture('example').then(function(data) {    
    this.data=data      
    })    
  })

    it('Login using fixture', function() {

        //Visit the Matchi AB  Website
        cy.visit('/');
        cy.get('a').then(link => {cy.request(link.prop('href')).its('status').should('eq', 200);});
           
        // Navigating to "login"
      cy.get('.navbar-right > :nth-child(2) > a > span').click()   
      cy.get('#username').type(this.data.email)
      cy.get("#password").type(this.data.password)
      cy.get('#loginForm > .checkbox > label').click()      
      cy.get('#loginForm > .btn').click()
     
       //cy.get('li.user-menu:nth-child(4) > a.dropdown-toggle').click()
       //.get('a').eq(1).click({force: true})

       //cy.get('li.user-menu:nth-child(4) > a.dropdown-toggle').click()
      //.get('ul.dropdown-menu').contains('Log out')
      //.click({force:true})  

      // price minus
      //body.profile.home.splash-page:nth-child(2) section.user-profile.block.vertical-padding20:nth-child(3) div.container div.row div.col-sm-8 div.panel.panel-default:nth-child(3) ul.list-group.alt li.list-group-item:nth-child(1) div.media 
      //div.pull-right div.bg-grey-light.border-radius.vertical-padding5.horizontal-padding10.text-center a:nth-child(1) > span.block.h3.no-margin.bold
      ////cy.get('[class="trailingSlotSelector"]')            .select('11:30')


      // test Badge information about booking in main webpage
      //cy.get('.badge').then($nbookings => {
       // const nbookings = parseFloat($nbookings.text())
       // cy.get('a.userCancelBooking').its('length').should('eq', nbookings)
      //})
     /*
      
      cy.get('.dropdown-toggle').eq(3).click()
      .get('a.userCancelBooking').first() 
      .click() 

      cy.get('a.btn.btn-md.btn-danger').first().click()
      cy.get('#cancelCloseBtn').click()
*/
    })
    
  
  
  it('Show all my booking ', function() {

// show all my Bookings
    cy.get('.dropdown-toggle').eq(3).click({force:true})
      .get('ul.dropdown-menu.dropdown-bookings').contains('Show all my bookings')
      .click({force:true})     
      
      
     //
     // cy.get('.dropdown-toggle').eq(3).click()
      //.get('.dropdown-menu'> 'a.userCancelBooking')
      //.click()      



      // test Badge information about booking in Show all my booking  webpage
      //cy.get('.badge-info').then($nbookings => {
       // const nbookings = parseFloat($nbookings.text())
       // cy.get('tr > .text-right').its('length').should('eq', nbookings)
      //})
      cy.wait(150)
})

it('Cancel first available  booking ', function() {

  // show all my Bookings
  //body.profile.bookings:nth-child(2) section.block.vertical-padding30:nth-child(2) div.container div.row div.col-sm-6:nth-child(1) div.row div.col-sm-12:nth-child(1) div.panel.panel-default div.table-responsive table.table.table-striped.text-sm tbody:nth-child(2) tr:nth-child(1) td.vertical-padding20.text-right:nth-child(5) 
 // a.btn.btn-link.btn-xs.btn-danger.text-danger > i.fas.fa-times
//  cy.get('.fas.fa-times').first().click({force:true})

  cy.get('a.btn.btn-link.btn-xs.btn-danger.text-danger').eq(1)
  .click({force:true})
 //cy.get('a.btn.btn-link.btn-xs.btn-danger.text-danger').first().contains('slotId','e4ea4f2075bc03f00175bc04d0760c64').click({force:true})
 cy.wait(150)
 
 //cy.get('div[class="table-responsive"]').contains('a').eq(0).click()
 //cy.get('a.btn.btn-link.btn-xs.btn-danger.text-danger > i.fas.fa-times').eq(0).click({force:true})
 //cy.get('table').contains('td','Paid')
 //cy.get('.fa-times').parent().eq(0).click()
 //cy.get('table').contains('td','Cancel').click()
 
 

 //jQuery.ajax({type:'POST',data:{'slotId': 'e4ea4f2075bc03f00175bc04d0760c64','returnUrl': 
  
  ///html[1]/body[1]/div[1]/div[3]/section[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr[1]/td[4]

  
 // jQuery.ajax({type:'POST',data:{'slotId': 'e4ea4f2075bc03f00175bc04d0570c0a',
      
      
  })
/*
it('Show past  booking ', function() {

  // show all my Bookings
  
  cy.get('div.col-sm-12').children().contains(' Show past bookings').click()
  
         
  
      
      
  })

  


  
it('Show all my Recording', function() {

  // show all my Recording
      cy.get('.dropdown-toggle').eq(3).click()
        .get('.dropdown-menu').contains('Show all my recordings')
        .should('have.text', 'Show all my recordings').click()  
               
            
  })

*/
})
