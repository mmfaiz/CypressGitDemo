package com.matchi.payment
/**
 * Exception thrown when error occurs in GenericPaymentController.processArticle(), due to some error creating article.
 */
class ArticleCreationException extends Exception {

    ArticleCreationException(String s) {
        super(s)
    }
}
