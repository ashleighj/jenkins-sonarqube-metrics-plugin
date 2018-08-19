#!/usr/bin/env groovy
package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.exceptions.DataIntegrityException
import com.travelstart.plugins.exceptions.GithubException
import com.travelstart.plugins.exceptions.SonarqubeException
import com.travelstart.plugins.utils.RestClient
import groovy.json.JsonSlurper

class Coverage extends Metric {
    final static COMPONENT = "component"
    final static METRIC_KEYS = "metricKeys"
    final static CONTEXT = "Code Coverage"

    RestClient sonarqubeClient

    Coverage(final String url, final String sonarToken, final String gitRepo, final String gitToken) {
        this.sonarqubeClient = new RestClient(hostname: url, token: sonarToken)
        this.context = CONTEXT
        this.repository = gitRepo
        this.gitToken = gitToken
    }

    def compare(final String prId, final List<String> projects, final boolean isNew = false) {
        final def metrics = retrieveCodeCoverageMetrics(projects, isNew)
        final def comparison = verifyCoverage(metrics)
        final def targetUrl = "${sonarqubeClient.hostname}/component_measures?id=${URLEncoder.encode(projects[0], "UTF-8")}&metric=coverage"

        return update(prId, comparison.state, targetUrl, comparison.message)
    }

    Double[] retrieveCodeCoverageMetrics(final List<String> projects, final boolean isNew = false) {
        if (!projects)
            throw new DataIntegrityException("At least one project ID should be provided")

        Double[] result = []

        projects.each {
            def final map = createUrlParams(it, isNew)
            def final parser = new JsonSlurper()
            def final response = sonarqubeClient.get( "/api/measures/component", map)

            // Verifies that it was successful, otherwise raises an Exception
            this.isSuccessful(response, SonarqubeException)

            def body = parser.parseText(response.content.text as String)
            result += retrieveValues(body, map)
        }

        return result
    }

    static Map<String, String> verifyCoverage(final Double[] metrics) {
        if (metrics[0] > metrics[1])
            return [message: "New code reduced the coverage from ${metrics[0]}% to ${metrics[1]}%", state: "failure"]
        else
            return [message: "New code increased the coverage from ${metrics[0]}% to ${metrics[1]}%", state: "success"]
    }

    static Map<String, String> createUrlParams(final String projectId, final boolean isNew) {
        def final map = [:]
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

    @Override
    def update(final String prId, final String state, final String targetUrl, final String description) {
        final def response = updateGithubPullRequestStatus(prId, state, targetUrl, description)
        final def parser = new JsonSlurper()

        // Verifies that it was successful, otherwise raises an Exception
        this.isSuccessful(response, GithubException)

        return parser.parseText(response.content.text as String)
    }
}
