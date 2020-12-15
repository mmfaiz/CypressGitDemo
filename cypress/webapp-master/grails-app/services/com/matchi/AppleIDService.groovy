package com.matchi

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.matchi.api.AppleIDAuthenticationCommand
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.joda.time.DateTime

import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPublicKeySpec

class AppleIDService {

    static transactional = false

    def userService
    def grailsApplication
    private static final APPLE_ID_URL = "https://appleid.apple.com"

    private getApiSettings() {
        def config = grailsApplication.config.signInWithApple
        return [appleAuthUrl: config.appleAuthUrl as String,
                clientId    : config.clientId as String,
                keyId       : config.keyId as String,
                teamId      : config.teamId as String,
                authKey     : config.authKey as String,
        ]
    }

    User getOrConnectUserByAppleIDProfile(AppleIDToken appleID, AppleIDAuthenticationCommand cmd) {
        def user = User.findByAppleUID(appleID.userIdentifier)

        // Check existing user
        if (user) {
            log.debug("Found existing apple user: ${user.fullName()}, ${user.email}")
            return user
        }

        // Check logged in user
        user = userService.getLoggedInUser()
        if (user) {
            log.debug("Found logged in user: ${user.fullName()}, ${user.email}, connecting apple user")
            connectWithExistingUser(user, appleID, cmd)
            return user
        }

        // Try find matching user by email and connect
        user = User.findByEmail(appleID.email)
        if (user) {
            log.debug("Found existing user to connect to AppleID: ${user.fullName()}, ${user.email}")
            // User already exists but is not connected with the particular apple ID user
            // lets connect and update them
            connectWithExistingUser(user, appleID, cmd)
            return user
        }

        // No user was found, register new user based on apple ID profile
        return register(appleID, cmd)
    }

    def connectWithExistingUser(User user, AppleIDToken appleID, AppleIDAuthenticationCommand cmd) {
        userService.addAppleIDToUser(user, appleID, cmd)
    }

    def register(AppleIDToken appleID, AppleIDAuthenticationCommand cmd) {
        userService.registerUserWithAppleID(appleID, cmd)
    }

    /*
    * Returns unique user id from apple
    * */

    AppleIDToken appleAuth(String authorizationCode) throws Exception {
        try {
            TokenResponse tokenResponse = validateAuthorizationCode(authorizationCode)
            String idToken = tokenResponse.getIdToken()
            String[] parts = idToken.split("\\.")
            JWTHeader header = new Gson().fromJson(new String(Decoders.BASE64.decode(parts[0])), JWTHeader.class)
            getClaims(header.getKid(), idToken)

            AppleIDToken idTokenPayload = new Gson().fromJson(new String(Decoders.BASE64.decode(parts[1])), AppleIDToken.class)

            return validateIdTokenPayload(idTokenPayload)
        } catch (Exception e) {
            log.error("failed to auth", e)
            return null
        }
    }

    private TokenResponse validateAuthorizationCode(String authorizationCode) throws Exception {
        HttpResponse<String> response = Unirest.post(apiSettings.appleAuthUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("client_id", apiSettings.clientId)
                .field("client_secret", generateJWT(getPrivateKey()))
                .field("grant_type", "authorization_code")
                .field("code", authorizationCode)
                .asString()

        TokenResponse tokenResponse = new Gson().fromJson(response.getBody(), TokenResponse.class)

        if (tokenResponse.error) {
            throw new Exception("Error with Apple Sign in: " + tokenResponse.error)
        }
        return tokenResponse
    }

    private PrivateKey pKey

    private PrivateKey getPrivateKey() throws Exception {
        final PEMParser pemParser = new PEMParser(new StringReader(apiSettings.authKey.replaceAll("%%", "\n")))
        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
        final PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject()
        final PrivateKey pKey = converter.getPrivateKey(object)

        return pKey
    }

    private PublicKey createPublicKeyApple(String keyIdentifier) throws Exception {
        // TODO Cache this reponse
        def response = Unirest.get("${APPLE_ID_URL}/auth/keys").asString()
        ListApplePublicKey applePublicKeysList = new Gson().fromJson(response.getBody(), ListApplePublicKey.class)

        Optional<ApplePublicKey> applePublicKey = applePublicKeysList.getKeys().stream()
                .filter({ publicKey -> keyIdentifier.equals(publicKey.getKid()) })
                .findFirst();

        if (!applePublicKey.isPresent()) throw new Exception("Not matching key");

        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(applePublicKey.get().getN()));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(applePublicKey.get().getE()));

        return KeyFactory.getInstance(applePublicKey.get().getKty()).generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }

    private Claims getClaims(String keyIdentifier, String idToken) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PublicKey publicKey = createPublicKeyApple(keyIdentifier);
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(idToken).getBody();
    }

    private AppleIDToken validateIdTokenPayload(AppleIDToken idToken) throws Exception {
        if (idToken.aud == apiSettings.clientId) {
            if (idToken.iss == APPLE_ID_URL) {
                if ((long) (idToken.exp) * 1000 > DateTime.now().getMillis()) {
                    return idToken
                }
            }
        }
        throw new Exception("Could not verify token")
    }

    private String generateJWT(PrivateKey pKey) throws Exception {
        String token = Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, apiSettings.keyId)
                .setIssuer(apiSettings.teamId)
                .setAudience(APPLE_ID_URL)
                .setSubject(apiSettings.clientId)
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24)))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(pKey, SignatureAlgorithm.ES256)
                .compact()

        return token
    }

    final class ListApplePublicKey {

        private final List<ApplePublicKey> keys;

        ListApplePublicKey(@JsonProperty("keys") List<ApplePublicKey> keys) {
            this.keys = keys;
        }

        List<ApplePublicKey> getKeys() {
            return keys;
        }
    }

    final class ApplePublicKey {
        private final String kty;
        private final String kid;
        private final String use;
        private final String alg;
        private final String n;
        private final String e;

        ApplePublicKey(@JsonProperty("kty") String kty,
                       @JsonProperty("kid") String kid,
                       @JsonProperty("use") String use,
                       @JsonProperty("alg") String alg,
                       @JsonProperty("n") String n,
                       @JsonProperty("e") String e) {
            this.kty = kty;
            this.kid = kid;
            this.use = use;
            this.alg = alg;
            this.n = n;
            this.e = e;
        }

        String getKty() {
            return kty;
        }

        String getKid() {
            return kid;
        }

        String getUse() {
            return use;
        }

        String getAlg() {
            return alg;
        }

        String getN() {
            return n;
        }

        String getE() {
            return e;
        }

        @Override
        String toString() {
            return "ApplePublicKey{" +
                    "kty='" + kty + '\'' +
                    ", kid='" + kid + '\'' +
                    ", use='" + use + '\'' +
                    ", alg='" + alg + '\'' +
                    ", n='" + n + '\'' +
                    ", e='" + e + '\'' +
                    '}';
        }

    }

    final class JWTHeader {
        private final String kid;
        private final String alg;

        JWTHeader(@JsonProperty("kid") String kid,
                  @JsonProperty("alg") String alg) {
            this.kid = kid;
            this.alg = alg;
        }

        String getKid() {
            return kid;
        }

        String getAlg() {
            return alg;
        }
    }

    final class TokenResponse {

        private String access_token;
        private String token_type;
        private Long expires_in;
        private String refresh_token;
        private String id_token;
        private String error;

        String getIdToken() {
            return id_token;
        }

        String getAccessToken() {
            return access_token;
        }

        String getTokenType() {
            return token_type;
        }

        Long getExpiresIn() {
            return expires_in;
        }

        String getRefreshToken() {
            return refresh_token;
        }

        String getError() {
            return error;
        }

        @Override
        String toString() {
            return "TokenResponse{" +
                    "idToken='" + id_token + '\'' +
                    ", accessToken='" + access_token + '\'' +
                    ", tokenType='" + token_type + '\'' +
                    ", expiresIn=" + expires_in +
                    ", refreshToken='" + refresh_token + '\'' +
                    '}';
        }
    }
}
