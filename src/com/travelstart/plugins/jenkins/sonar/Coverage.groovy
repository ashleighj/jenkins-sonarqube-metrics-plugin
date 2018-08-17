package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.traits.HandleException
import com.travelstart.plugins.utils.RestClient
import groovy.json.internal.JsonParserCharArray

class Coverage implements HandleException{
    RestClient client

    Coverage(final String url, final String token) {
        this.client = new RestClient(hostname: url, token: token)
    }

    Double[] retrieveCodeCoverageMetrics(final List<String> projects) {
        final def result = []

        projects.each {
            def final map = new TreeMap()
            map.put("component", it)
            map.put("metricKeys", "coverage")

            def parser = new JsonParserCharArray()
            def response = client.get( "/api/measures/component", map)

            // Verifies that it was successful, otherwise raises an Exception
            isSuccessful(response)

            def body = parser.parse(response.content as InputStream)
            def rawValue = body.component.measures[0].value as String

            if (rawValue.isNumber()) {
                final Double value =  body.component.measures[0].value as Double
                result.add(value)
            }
            else {
                throw new Exception("Value is not a number")
            }
        }
        return result
    }
}
