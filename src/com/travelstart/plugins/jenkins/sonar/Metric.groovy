#!/usr/bin/groovy
package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.exceptions.GithubException
import com.travelstart.plugins.exceptions.PluginException
import com.travelstart.plugins.exceptions.SonarqubeException
import com.travelstart.plugins.utils.RestClient
import groovy.json.JsonOutput

abstract class Metric {
    String gitHostname = "https://api.github.com/repos"
    String context
    String repository
    String gitToken

    static void isSuccessful(final HttpURLConnection urlConnection, final Class cls) {
        if (!(urlConnection.responseCode in 200..300)) {
            final PluginException e

            if (cls == SonarqubeException)
                e = new SonarqubeException()
            else
                e = new GithubException()

            e.message = urlConnection.responseMessage
            e.code = urlConnection.responseCode

            try {
                e.body = urlConnection.content
            } catch (Exception ex) {
                e.body = ex.message
            }

            throw e
        }
    }

    def updateGithubPullRequestStatus(final String prId, final String state,
                                      final String targetUrl, final String description) {
        def params = [access_token: gitToken]
        def body = [state: state, target_url: targetUrl, context: context, description: description]
        def githubClient = new RestClient(hostname: gitHostname)

        return githubClient.post("${repository}/statuses/${prId}", params, JsonOutput.toJson(body))
    }

    abstract def update(final String prId, final String state, final String targetUrl, final String description)
}