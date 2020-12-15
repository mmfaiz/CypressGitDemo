describe('Loggin and book', () => {
    it('Test Loggin and book', () => {
      cy.visit('/login/auth?returnUrl=%2Fprofile%2Fhome')  
      //loggin, input username, password, test username on the page.
      cy.get('input[name="j_username"]').eq(0).type('abc001@gmail.com')
      cy.get('input[name="j_password"]').eq(0).type('1234567').should('have.value','1234567')
      cy.wait(500)
      cy.get('#loginForm > .btn').eq(0).click()
      cy.get('.col-sm-4 > .media > .media-body > .media-heading > a').should('contain','Anna')
      
      cy.get('#acceptConsentModalCheckBoxTerms').click({force: true}) 
      cy.get('#acceptConsentModal > .modal-dialog > .modal-content > .modal-footer').contains('Accept').click({force: true} )
    
      cy.get(':nth-child(3) > .dropdown-toggle > span').click()
      cy.get('.navbar-right > .open > .dropdown-menu > :nth-child(1) > a').then(elem => {
       
        // elem is a jQuery object
        console.log(elem.text());
        let BookingNumber;
        let BookingNumberNow;
        if (elem.text() == 'You have no upcoming bookings') {
            // do something
            BookingNumber=0
        }
        else {
            // ..
            cy.get(':nth-child(1) > .badge').then(($btn) => {
            // store the label's text
             BookingNumber = $btn.text()
          }) 
        }   
       
            // Book
            cy.get('.col-sm-4 > :nth-child(4) > .btn').click()
            
            //choose indoors, sport, club's name, then smash
            cy.get('#inOutCourt').select('Indoors',{ force: true })
            cy.get('#showDate')
              .invoke('val', '2020-12-16')
              .trigger('change')
            cy.get('#q').click().type("Lundby-Biskopsgården Klubb") 
            cy.get('.col-sm-2 > .btn').eq(0).click();
            cy.get('.media-heading > a').should('contain','Lundby-Biskopsgården Klubb');
        
            cy.wait(100)
        //choose time and slots          
            cy.get('.no-margin.no-padding').first().click()  
            cy.get('.slot.free').first().click({force: true})
            
            cy.get('[class="btn btn-md btn-success"]').click() // submit payment
        
            cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer > .btn').click() // finish booking
            // compare the two texts
            // and make sure they are different
            cy.get(':nth-child(1) > .badge').should(($btn2) => {
               expect($btn2.text()).not.to.eq(BookingNumber)
            })
            
            cy.get(':nth-child(3) > .dropdown-toggle > :nth-child(1)')
              .get('a[href*="bookings"]').contains('Show all my bookings').click({ force: true }) // SHow all bookings
            
            cy.get(':nth-child(1) > :nth-child(1) > :nth-child(1) > .panel > .panel-heading > .row > .col-sm-3').click()
              .get('.col-sm-3 > .badge').should(($btn3) => {
                expect($btn3.text()).not.to.eq(BookingNumber)
            }) 

            //delete a booking
            cy.get(':nth-child(1) > .text-right > .btn').click()
            cy.get('#userBookingModal > .modal-dialog > .modal-content > .modal-footer')
              .get('.btn-md').contains('Cancel').click({ force: true })
              cy.get('#cancelCloseBtn').click({ force: true })

            // compare the two texts
            // and make sure they are same
            cy.get(':nth-child(1) > .badge').should(($btn2) => {
              if($btn2.text()=="") {
                  BookingNumberNow=0
                  expect(BookingNumberNow).to.eq(BookingNumber)
              }else{
                BookingNumberNow=$btn2.text
                expect($btn2.text()).to.eq(BookingNumber)
              } 
           })
           
           cy.get(':nth-child(1) > :nth-child(1) > :nth-child(1) > .panel > .panel-heading > .row > .col-sm-3').click()
           .get('.col-sm-3 > .badge').should(($btn3) => {
            if($btn3.text()=="") {
              BookingNumberNow=0
              expect(BookingNumberNow).to.eq(BookingNumber)
          }else{
            BookingNumberNow=$btn3.text
            expect($btn3.text()).to.eq(BookingNumber)
          }  
         })


            
          
          
      })         
   })
  })
