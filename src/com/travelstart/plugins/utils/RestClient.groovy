package com.travelstart.plugins.utils

// https://sites.google.com/a/athaydes.com/renato-athaydes/code/groovy---rest-client-without-using-libraries
class RestClient {
    String hostname
    String token


    def get(final String endpoint, final Map<String, String> params = new TreeMap<>()) {
        return setupConnection(hostname + endpoint + setupUrlParams(params))
    }

    protected String setupUrlParams(final Map<String, String> params = new TreeMap<>()) {
        if (params) {
            return "?" + params.collect { k,v -> "$k=$v" }.join('&')
        }
        else {
            return ""
        }
    }

    protected HttpURLConnection setupConnection(final String url) {
        final def httpConnection = new URL(url).openConnection() as HttpURLConnection

        if (token) {
            final def encoded = "${token}:".bytes.encodeBase64().toString()
            httpConnection.setRequestProperty("Authorization", "Basic ${encoded}")
        }

        httpConnection.setRequestProperty("Content-Type", "application/json")

        return httpConnection
    }

}
