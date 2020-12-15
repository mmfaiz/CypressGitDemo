package com.matchi.admin

import grails.converters.JSON

class AdminEncodingController {

    def index() {
        [
                dangerData: "<script>alert('Dangerous script');</script>",
                goodData: "Name of a user",
                dangerJSData: "0; alert();",
                goodJSData: "Sune User Andersson ÅÄÖ123%&/",
                dangerTagData: "javascript:alert('Danger');",
                goodTagData: "http://localhost:8080/stuff?variable1=value&anotherParameter=4",
                dangerHTML: "<strong>This is some änna dåva HTML åså<script>alert('Danger!')</script></strong>",
                dangerHTMLInTag: "<strong onclick=\"javascript:alert('Danger!');\">This is some änna dåva HTML åså</strong>",
                goodJSON: ['Mattias', 'Victor', 'Daniel'] as JSON,
                badJSON: ['"]alert("Danger");")"', 'Victor', 'Daniel'] as JSON,
                dangerJSHTML: "<strong>This is some änna dåva HTML åså</strong>;alert();",
                encodedAlert: "<script>alert('This does not run!');</script>",
                notEncodedAlert: "<script>alert('This should run!');</script>",
                justList: ['Mattias', 'Victor', 'Daniel'],
                slotIds: ['edca8b9a375ba54a01375baccc4b0000', 'edca8b9a375ba54a01375baccc4f0001', 'edca8b9a375ba54a01375baccc530002'] as JSON,
                dangerTagData2: '" onclick="javascript:alert("WOHO");" data-tennis="',
                cookieStealingUrl: 'http://localhost:8080/stuff?variable1=" + document.cookie; var x = "hej'
        ]
    }
}
