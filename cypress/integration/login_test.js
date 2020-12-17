

// Successful login 


describe('Matchi AB Login Test Automation ', function() {

    it('Successful Login', function() {
  
          
        cy.visit('/');                //Visit the Matchi AB  Website
        cy.get('a').then(link => {cy.request(link.prop('href')).its('status').should('eq', 200);});
             
         
        cy.get('li.login').first().contains('Log in').click()        // Navigating to "login"
        cy.get('#username').type('abc0001@gmail.com')
        cy.get("#password").type('12345678')
        cy.get('#loginForm > .checkbox > label').click()      
        cy.get('#loginForm > .btn').click()       
        
        cy.get('li.user-menu:nth-child(4) > a.dropdown-toggle').click()
          .get('ul.dropdown-menu').contains('Log out')
          .click({force:true})  
  
        
  
      })
      

      it('Incorrect Login information provided', function() {
  
        //Visit the Matchi AB  Website
        cy.visit('/');
        cy.get('a').then(link => {cy.request(link.prop('href')).its('status').should('eq', 200);});
           
        // Navigating to "login"
       
        cy.get('li.login').first().contains('Log in').click()   
        cy.get('#username').type('abc0001@gmail.com')
        cy.get("#password").type('1234567890')
        cy.get('#loginForm > .checkbox > label').click()      
        cy.get('#loginForm > .btn').click()
   

    })   


    it('Test Registeration  with already registered Email', function() {   
        
        // Test with Registered email that already exist
        cy.get('.navbar-right > :nth-child(3) > a > span').click();
       // cy.get('li.login').eq(1).contains('Registrera dig').click({force:true}) // click on Registrera dig här link
        
        cy.get('#email').type('abc0001@gmail.com')
        cy.get("#firstname").type('abc001')
        cy.get('#lastname').type('abc001')
        cy.get('#password').type('12345678')
        cy.get('#password2').type('12345678')  
       // cy.get('#recaptcha-anchor').click()
        cy.get('#saveButton').click();
        cy.get('div.checkbox > label').first().click()
        cy.get('#acceptConsentModalSubmit').click();

    })

    it('Test Registeration with new credential', function() {   
        
        // Test with Registered email that already exist

       // cy.get('li.login').contains('Registrera dig').click() // click on Registrera dig här link
        
        cy.get('#email').clear().type('xyz0001@gmail.com')
        cy.get("#firstname").clear().type('xyz001')
        cy.get('#lastname').clear().type('xyz001')
        cy.get('#password').clear().type('12345678')
        cy.get('#password2').clear().type('12345678')  
       // cy.get('#recaptcha-anchor').click()
        cy.get('#saveButton').click();
        cy.get('div.checkbox > label').first().click()
        cy.get('#acceptConsentModalSubmit').click();

    })

    it('Test Registeration with wrong email address', function() {   
        
        // Test with Registered email that already exist

       // cy.get('li.login').contains('Registrera dig').click() // click on Registrera dig här link
        
        cy.get('#email').clear().type('xyz0001@gmail.com0012')
        cy.get("#firstname").clear().type('xyz001')
        cy.get('#lastname').clear().type('xyz001')
        cy.get('#password').clear().type('12345678')
        cy.get('#password2').clear().type('12345678')  
       // cy.get('#recaptcha-anchor').click()
        cy.get('#saveButton').click();
        cy.get('div.checkbox > label').first().click()
        cy.get('#acceptConsentModalSubmit').click();

    })



it(' Login with new Username and Password', function() {
                   
    //
        cy.get('div.container').contains('Logga in här').click()         // Navigating to "login"

        cy.get('#username').type('xyz0001@gmail.com')
        cy.get("#password").type('12345678')
        cy.get('#loginForm > .checkbox > label').click()      
        cy.get('#loginForm > .btn').click()       
        
        cy.get('li.user-menu:nth-child(4) > a.dropdown-toggle').click()
          .get('ul.dropdown-menu').contains('Log out')
          .click({force:true})  
  
        
  
      })
      


     it('Test Forget User Password', function() {               


        cy.get('div.container').contains('Logga in här').click()         // Navigating to "login"
        cy.get('a.forgot.right').click() // forget password
        cy.get('#email_reset').type('abc0001@gmail.com')
        cy.get('#formSubmit').click()
        cy.get('a.btn-success.btn-large').contains('Tillbaka till startsidan').click()      

    })

    it('Explore Matchi AB Home Page ', function() {
        
        
        // Navigating to "Booking" 
        cy.get('ul.nav.navbar-nav.navbar-left').contains('Boka').click();
              
         // Navigating to "Activities"
        cy.get('ul.nav.navbar-nav.navbar-left').contains('Aktiviteter').click({force:true});
       
        // Navigating to "Venues"
        cy.get('ul.nav.navbar-nav.navbar-left').contains('Anläggningar').click({force:true});
        
        // Navigating to "For the Club"
        cy.get('ul.nav.navbar-nav.navbar-left').contains('För klubben').click({force:true});
        
        // Navigating to "Language"
       
        cy.get('a.dropdown-toggle').click({force:true});
        
        // Navigating to "login"
        cy.get('ul.nav.navbar-nav.navbar-right').contains('Logga in').click({force:true});
        
        // Navigating to "Registration"
       // cy.get('.navbar-right > :nth-child(3) > a > span').click();
        cy.get('ul.nav.navbar-nav.navbar-right').contains('Registrera dig').click({force:true})
    
        
      })
    


    })
      
    
    