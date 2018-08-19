#!/usr/bin/groovy
package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.exceptions.SonarqubeException
import com.travelstart.plugins.utils.RestClient

abstract class Metric {

    String context
    String repository // sh(returnStdout: true, script: 'git config remote.origin.url').trim()
    String gitToken

    static void isSuccessful(final HttpURLConnection urlConnection) {
        if (!(urlConnection.responseCode in 200..300)) {
            final SonarqubeException e = new SonarqubeException()
            e.message = urlConnection.responseMessage
            e.code = urlConnection.responseCode
            e.body = urlConnection.content

            throw e
        }
    }

    def updateGithubPullRequestStatus(final String prId, final String authToken, final String state,
                                      final String targetUrl, final String description) {
        def githubClient = new RestClient(hostname: "https://api.github.com/repos")
        githubClient.post()
    }

}