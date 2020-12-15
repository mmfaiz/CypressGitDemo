package com.matchi.devices

import com.matchi.User
import org.apache.commons.lang.RandomStringUtils

class Device {

    static belongsTo = [user: User]
    static hasMany   = [tokens: Token]

    String deviceId
    String deviceModel
    String deviceDescription

    Date lastUsed
    Date blocked
    Date dateCreated
    Date lastUpdated

    def getModelShortname() {
        return "iphone"
    }

    def getValidToken() {
        def validTokens = tokens.findAll { it.isValid() }
        if(validTokens.isEmpty()) {
            return createNewToken()
        } else {
            validTokens.iterator().next()
        }
    }

    private def createNewToken() {
        def maxIdentifierGenerationCount = 100
        def count = 0
        def token = new Token(device: this, identifier: Token.generateIdentifier())

        // check that identifier does not exists
        while(Token.findByIdentifier(token.identifier) != null) {

            if(count > maxIdentifierGenerationCount) {
                throw new IllegalStateException("Could not generate new token (tried ${maxIdentifierGenerationCount} times...)")
            }

            token.identifier = Token.generateIdentifier()

            count++;
        }

        token.save()
        token.device = this
        this.addToTokens(token)

        return token
    }

    static constraints = {
        deviceId unique: 'user'
        deviceModel blank: false
        deviceDescription blank: false
        blocked nullable: true
        lastUsed nullable: true
    }

    static mapping = {
        autoTimestamp true
        tokens sort: "dateCreated", order: "asc"
    }
}
