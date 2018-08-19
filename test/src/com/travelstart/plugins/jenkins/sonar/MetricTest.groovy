package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.BaseTest
import com.travelstart.plugins.exceptions.SonarqubeException
import groovy.json.JsonSlurper
import spock.lang.Shared

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock

class MetricTest extends BaseTest {
    @Shared def metric = new MetricImpl()
    @Shared def parser = new JsonSlurper()

    def setup() {
        setupServer()
    }

    void cleanup() {
        cleanupServer()
    }

    def "Raise An Exception if HTTP response status code is not between 200 and 299"() {
        given:
            def response = mock(HttpURLConnection.class)

            doReturn(404).when(response).responseCode
            doReturn("Not Found").when(response).responseMessage
            doReturn(importFile("404.json").text).when(response).content

        when:
            Metric.isSuccessful(response, SonarqubeException)

        then:
            def e = thrown(SonarqubeException)
            def expectedResponse = importFile("404.json").text

            assertThat(e.code, equalTo(404))
            assertThat(e.message, equalTo("Not Found"))
            assertThat(e.body, equalTo(expectedResponse))
    }

    def "Continue without failure if HTTP response code is between 200 and 299"() {
        given:
            def response = mock(HttpURLConnection.class)

        when:
            (200..300).each {
                doReturn(200).when(response).responseCode
                Metric.isSuccessful(response, SonarqubeException)
            }

        then:
            notThrown(Exception)
    }

    def "Change Github PR Status to success"() {
        given:
            def request = parser.parseText(importFile("github-status-request-success.json").text)
            def prId = "435aaa4232342bbb"
            def metricImpl = new MetricImpl(hostname as String)

            generateGithubResponse(prId, "github-status-request-success.json", "github-status-response-success.json", metricImpl.gitToken)

        when:
            def response = metricImpl.updateGithubPullRequestStatus(prId, request.state, request.target_url, request.description)

        then:
            def body = parser.parseText(response.content.text as String)
            assertThat(response.responseCode, equalTo(201))
            assertThat(body.id, notNullValue())
            assertThat(body.url, notNullValue())
            assertThat(body.state, equalTo(request.state))
            assertThat(body.description, equalTo(request.description))
            assertThat(body.target_url, equalTo(request.target_url))
            assertThat(body.context, equalTo(request.context))
            assertThat(body.creator, notNullValue())

    }

    class MetricImpl extends Metric {
        MetricImpl() {
            context = "Mock Metric"
            repository = "/mock/repo"
            gitToken = "ab58dbai399"
        }

        MetricImpl(final String gitHostname) {
            this()
            this.gitHostname = gitHostname
        }

        @Override
        def update(final String prId, final String state, final String targetUrl, final String description) {
            return super.update(prId, state, targetUrl, description)
        }
    }
}

