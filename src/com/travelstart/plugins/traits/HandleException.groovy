#!/usr/bin/groovy
package com.travelstart.plugins.traits

import com.travelstart.plugins.exceptions.SonarqubeException

trait HandleException {

    void isSuccessful(final HttpURLConnection urlConnection) {
        if (!(urlConnection.responseCode in 200..300)) {
            final SonarqubeException e = new SonarqubeException()
            e.message = urlConnection.responseMessage
            e.code = urlConnection.responseCode
            e.body = urlConnection.content

            throw e
        }
    }

}