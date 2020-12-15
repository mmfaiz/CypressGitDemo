package com.matchi

// The purpose of this controller is to fetch the output from an external url. One scenario would be when mixing in new
// front-end architecture. Example setup in UrlMappings.groovy:
// "/about"(controller: "contentFromUrl", action:"index") { url = "http://www.dn.se/sport/" }

class ContentFromUrlController extends GenericController {

    private static String SCRIPT_IDENTIFIER = "script type=\"text/javascript\" src=\""

    def index() {
        String baseurl = params.baseurl
        String path = params.path
        if(!baseurl || !path)
            render "Missing parameter 'baseurl' or 'path'"

        URL fullUrl = (baseurl + path).toURL()
        render fullUrl.getText()
            .replaceAll(SCRIPT_IDENTIFIER + "/", SCRIPT_IDENTIFIER + baseurl) // Update script src tags.
            //.replaceAll("/./", "/") // Update paths.
    }
}
