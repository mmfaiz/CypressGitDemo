package com.matchi.api

import com.matchi.InvoiceService
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.CompileStatic
import org.springframework.util.StopWatch

/**
 * @author Sergei Shushkevich
 */
@CompileStatic
class ArticleResourceController extends GenericAPIController {

    InvoiceService invoiceService

    def listArticles() {
        def facility = requestFacility
        def articles = invoiceService.getItems(facility)
        log.info("Getting ${articles.size()} articles for ${facility.name}")
        render articles as JSON
    }

    def updateArticles() {
        def facility = requestFacility

        // Only supposed to be used where articles are pushed to MATCHi, i.e. NOT Fortnox.
        if (!facility.hasExternalArticles()) {
            error(400, Code.INPUT_ERROR, "Bad request")
            return
        }

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        def cmds = requestJSONArray.collect {
            def cmd = new ArticleCommand()
            bindData(cmd, it)
            cmd
        }
        cmds.each {ArticleCommand cmd -> cmd.validate()}
        if (cmds.any {ArticleCommand cmd -> cmd.hasErrors()}) {
            renderValidationErrors(cmds*.errors)
            return
        }

        invoiceService.updateItems(facility?.id, cmds)

        stopWatch.stop()
        log.info("Updated ${requestJSONArray.size()} articles for ${facility?.name} in ${stopWatch.totalTimeMillis} ms.")

        render([:] as JSON)
    }
}

@Validateable(nullable = true)
class ArticleCommand {

    Long articleNumber
    String name
    String description
    Float price
    Integer vat

    static constraints = {
        articleNumber nullable: false
        name nullable: false, blank: false
        description nullable: true
        price nullable: true
        vat nullable: true
    }


    @Override
    public String toString() {
        return "ArticleCommand{" +
                "articleNumber=" + articleNumber +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", vat=" + vat +
                '}';
    }
}