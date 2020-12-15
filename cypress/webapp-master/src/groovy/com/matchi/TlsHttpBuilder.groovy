package com.matchi

import groovyx.net.http.HTTPBuilder
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.impl.conn.SchemeRegistryFactory
import org.apache.http.params.HttpParams

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import java.security.SecureRandom

/**
 * HTTPBuilder that allows the list of supported TLS protocols to be specified.
 */
class TlsHttpBuilder extends HTTPBuilder {

    List sslProtocols

    TlsHttpBuilder(List sslProtocols) {
        super()

        this.sslProtocols = sslProtocols
    }

    protected HttpClient createClient(HttpParams params) {

        def sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, null, new SecureRandom())

        def sf = new org.apache.http.conn.ssl.SSLSocketFactory(sslContext) {

            protected void prepareSocket(final SSLSocket socket) throws IOException {
                if (sslProtocols) {
                    log.debug("Setting protocols: ${sslProtocols}")

                    socket.setEnabledProtocols(sslProtocols as String[])
                }
            }
        }

        def schemeRegistry = SchemeRegistryFactory.createDefault()
        schemeRegistry.register(new org.apache.http.conn.scheme.Scheme("https", sf, 443))

        new DefaultHttpClient(new PoolingClientConnectionManager(schemeRegistry), params)
    }
}

