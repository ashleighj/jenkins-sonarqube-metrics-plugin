#!/usr/bin/groovy
package com.travelstart.plugins.utils

class RestClient {
    String hostname
    String token


    def get(final String endpoint, final Map<String, String> params = [:]) {
        return setupConnection(hostname + endpoint + setupUrlParams(params))
    }

    static String setupUrlParams(final Map<String, String> params) {
        if (params) {
            return "?" + params.collect { k,v -> "$k=$v" }.join('&')
        }
        else {
            return ""
        }
    }

    HttpURLConnection setupConnection(final String url) {
        final def httpConnection = new URL(url).openConnection() as HttpURLConnection

        if (token) {
            final def encoded = "${token}:".bytes.encodeBase64().toString()
            httpConnection.setRequestProperty("Authorization", "Basic ${encoded}")
        }

        httpConnection.setRequestProperty("Content-Type", "application/json")

        return httpConnection
    }

}
