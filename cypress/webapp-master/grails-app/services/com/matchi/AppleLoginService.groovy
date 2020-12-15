package com.matchi

import com.amazonaws.services.sns.model.AuthorizationErrorException
import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import com.matchi.api.Code
import grails.converters.JSON
import grails.util.Holders
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import com.mashape.unirest.http.HttpResponse
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.security.authentication.BadCredentialsException

import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException

// Example code from:
// https://stackoverflow.com/questions/58252089/sign-in-with-apple-java-user-verification

class AppleLoginService {

    static transactional = false

}