package com.matchi

import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest

import java.util.regex.Matcher
import java.util.regex.Pattern

class FrontEndMessageService {

    static transactional = false

    def userService

    String getHTML(FrontEndMessage frontEndMessage) {

        String html = frontEndMessage.htmlContent
        User user = userService.getLoggedInUser()

        while(true)
        {
            Matcher i = Pattern.compile('(\\{image=)([^}]+)(})').matcher(html)
            if (i.find()) {
                String imageName = i.group(2)
                if (!imageName) {
                    break
                }
                MFile image = frontEndMessage.imagesList?.get(imageName)

                String replaceString

                if (image) {
                    replaceString = image.getAbsoluteFileURL()
                }
                else {
                    //Writes out just the name to notice error
                    replaceString = i.group(2)
                }

                html = html.substring(0, i.start()) + replaceString + html.substring(i.end())
            }
            else {
                break
            }
        }

        while(true)
        {
            Matcher t = Pattern.compile('(\\{text(\\[([^]]+)]|)=)([^}]+)(})').matcher(html)
            if (t.find()) {
                String languagesString = t.group(3)
                Boolean negativeLang = false
                List<String> languages

                if (languagesString) {
                    if (languagesString.substring(0, 1) == "!") {
                        negativeLang = true
                        languagesString = languagesString.substring(1)
                    }
                    languages = languagesString.toLowerCase().split(",")
                }
                String language = user?.language

                if (!language) {
                    language = GrailsWebRequest.lookup()?.getLocale().language
                }

                String replaceString = ""
                if (languages && language) {
                    if ((!negativeLang && languages?.contains(language)) || (negativeLang && !languages?.contains(language))) {
                        replaceString = t.group(4)
                    }
                }
                else {
                    replaceString = t.group(4)
                }

                html = html.substring(0, t.start()) + replaceString + html.substring(t.end())
            }
            else {
                break
            }
        }



        return html

    }
}
