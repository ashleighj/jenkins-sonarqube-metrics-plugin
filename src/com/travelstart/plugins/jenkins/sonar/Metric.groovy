#!/usr/bin/groovy
package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.exceptions.SonarqubeException

abstract class Metric {

    static void isSuccessful(final HttpURLConnection urlConnection) {
        if (!(urlConnection.responseCode in 200..300)) {
            final SonarqubeException e = new SonarqubeException()
            e.message = urlConnection.responseMessage
            e.code = urlConnection.responseCode
            e.body = urlConnection.content

            throw e
        }
    }

}