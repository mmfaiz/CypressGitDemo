package com.matchi.fortnox.v3

import com.matchi.Facility
import com.matchi.facility.Organization

import java.lang.reflect.Field

/**
 * @author Michael Astreiko
 */
class Fortnox3ArticleService {
    static transactional = false
    private static final String ARTICLES = "articles"

    def fortnox3Service

    /**
     *
     * @return
     */
    List<FortnoxArticle> list(Facility facility, Integer page = null) {
        def result = []
        //Max possible based on http://developer.fortnox.se/documentation/general/parameters/
        def requestParams = ['limit': 500, 'filter':'active']
        if (page) {
            requestParams['page'] = page
        }
        fortnox3Service.doGet(facility, ARTICLES, null, requestParams) { def responseJSON ->
            responseJSON.Articles?.each { articleJSON ->
                result << new FortnoxArticle(getArticlePropertiesBasedOnJSON(articleJSON))
            }

            if (responseJSON.MetaInformation.'@TotalPages' > responseJSON.MetaInformation.'@CurrentPage') {
                result += list(facility, responseJSON.MetaInformation.'@CurrentPage' + 1)
            }
        }
        result
    }

    /**
     *
     * @return
     */
    List<FortnoxArticle> listForOrganization(Organization organization, Integer page = null) {
        def result = []
        //Max possible based on http://developer.fortnox.se/documentation/general/parameters/
        def requestParams = ['limit': 500, 'filter':'active']
        if (page) {
            requestParams['page'] = page
        }
        fortnox3Service.doGetForOrganization(organization, ARTICLES, null, requestParams) { def responseJSON ->
            responseJSON.Articles?.each { articleJSON ->
                result << new FortnoxArticle(getArticlePropertiesBasedOnJSON(articleJSON))
            }

            if (responseJSON.MetaInformation.'@TotalPages' > responseJSON.MetaInformation.'@CurrentPage') {
                result += listForOrganization(organization, responseJSON.MetaInformation.'@CurrentPage' + 1)
            }
        }
        result
    }

    /**
     *
     * @param articleNumber
     * @return
     */
    FortnoxArticle get(Facility facility, String articleNumber) {
        FortnoxArticle result = null
        fortnox3Service.doGet(facility, ARTICLES, articleNumber) { def articleJSON ->
            result = new FortnoxArticle(getArticlePropertiesBasedOnJSON(articleJSON.Article))
        }
        result
    }

    /**
     *
     * @param article
     * @return
     */
    FortnoxArticle set(Facility facility, FortnoxArticle article) {
        FortnoxArticle result = null
        Map articleMap = [:]
        FortnoxArticle.class.getDeclaredFields().each { Field property ->
            if (article[property.name] && property.getModifiers() == 2) {
                articleMap[property.name] = article[property.name]
            }
        }
        def requestBody = [Article: articleMap]
        fortnox3Service.doPost(facility, ARTICLES, requestBody) { def articleJSON ->
            log.info("Created customer with name ${articleJSON.Article.ArticleNumber}")
            result = new FortnoxArticle(getArticlePropertiesBasedOnJSON(articleJSON.Article))
        }
        return result
    }

    /**
     *
     * @param articleJSON
     * @return
     */
    private LinkedHashMap getArticlePropertiesBasedOnJSON(articleJSON) {
        def availableFieldNames = FortnoxArticle.class.getDeclaredFields()*.name
        def articleProperties = [:]
        articleJSON.entrySet().each {
            if (availableFieldNames.contains(it.key) && !it.key.startsWith('@') && !['DefaultDeliveryTypes', 'DefaultTemplates'].contains(it.key)) {
                //Few fields on List view differ from detail view
                if (it.key == 'Phone') {
                    articleProperties['Phone1'] = it.value
                } else if (it.key == 'VAT' && it.value?.getClass() != net.sf.json.JSONNull && it.value) {
                    articleProperties[it.key] = new BigDecimal(it.value).toFloat()
                }  else if (it.value == '0,00') {//workaround for Fortnox API bug
                    articleProperties[it.key] = 0
                } else if (it.value == '1.0000') {//workaround for another Fortnox API bug :)
                    articleProperties[it.key] = 1
                } else {
                    articleProperties[it.key] = it.value?.getClass() == net.sf.json.JSONNull ? null : it.value
                }
            }
        }
        articleProperties
    }
}
