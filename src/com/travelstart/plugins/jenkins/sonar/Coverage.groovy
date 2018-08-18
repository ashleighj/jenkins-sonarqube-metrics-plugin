#!/usr/bin/env groovy
package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.exceptions.DataIntegrityException
import com.travelstart.plugins.utils.RestClient
import groovy.json.internal.JsonParserCharArray

class Coverage extends Metric {
    final static COMPONENT = "component"
    final static METRIC_KEYS = "metricKeys"

    RestClient client

    Coverage(final String url, final String token) {
        this.client = new RestClient(hostname: url, token: token)
    }

    Double[] retrieveCodeCoverageMetrics(final List<String> projects, final boolean isNew = false) {
        if (!projects)
            throw new DataIntegrityException("At least one project ID should be provided")
        final def result = []

        projects.each {
            def final map = createUrlParams(it, isNew)
            def final parser = new JsonParserCharArray()
            def final response = client.get( "/api/measures/component", map)

            // Verifies that it was successful, otherwise raises an Exception
            isSuccessful(response)

            def body = parser.parse(response.content as InputStream)

            result.addAll(retrieveValues(body, map))
        }
        return result
    }

    static Map<String, String> createUrlParams(final String projectId, final boolean isNew) {
        def final map = new TreeMap()
        map.put(COMPONENT, projectId)
        map.put(METRIC_KEYS, isNew? "coverage,new_coverage" : "coverage")

        return map
    }

    static Double[] retrieveValues(final body, final Map<String, String> params) {
        final def result = []
        final def measures = body?.component?.measures as List
        final def metrics = params.get(METRIC_KEYS).split(",")

        metrics.each {
            final def metric = it
            final def measure = measures.find { it.metric == metric }
            final def rawValue = (it == "coverage" ? measure?.value : measure?.periods[0]?.value) as String

            if (rawValue != null && rawValue.isNumber()) {
                result.add((rawValue as Double).round(2))
            }
            else {
                throw new DataIntegrityException(body.toString())
            }
        }

        return result
    }
}
