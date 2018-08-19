package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.BaseTest
import com.travelstart.plugins.exceptions.DataIntegrityException
import com.travelstart.plugins.exceptions.GithubException
import org.mockserver.model.Parameter
import spock.lang.*

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.mockito.Mockito.spy
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

class CoverageTest extends BaseTest {
    Coverage coverage

    @Shared def token = "1234567890"
    @Shared def gitToken = "1234567890"
    @Shared def prId = "435aaa4232342bbb"

    def setup() {
        setupServer()
        coverage = spy(new Coverage(hostname, token, gitRepo, gitToken))
    }

    void cleanup() {
        cleanupServer()
    }

    void generateOKCoverageResponses(final String component, final String path, final String metrics) {
        final def urlParams = [new Parameter("component", component), new Parameter("metricKeys", metrics)]
        final def body = importFile(path).text

        //https://localhost:{port}/api/measures/component?component={component}&metricKeys=coverage
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/measures/component")
                        .withQueryStringParameters(urlParams)
                        .withHeader("Authorization", "Basic MTIzNDU2Nzg5MDo="))
                .respond(
                response()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatusCode(200)
        )
    }

    void generateOKCoverageResponses(final String component, final String path) {
        generateOKCoverageResponses(component, path, "coverage")
    }

    def "Coverage Object was build with the correct parameters"() {
        when:
            coverage

        then:
            assertThat(coverage.context, equalTo(Coverage.CONTEXT))
            assertThat(coverage.repository, equalTo(gitRepo))
            assertThat(coverage.gitToken, equalTo(gitToken))
            assertThat(coverage.sonarqubeClient, notNullValue())
            assertThat(coverage.sonarqubeClient.token, equalTo(token))
            assertThat(coverage.sonarqubeClient.hostname, equalTo(hostname as String))
    }

    def "Coverage Object was build with the correct parameters even with final / at the end of the hostname"() {
        when:
            def coverage = new Coverage(hostname + "/", token, gitRepo, gitToken)

        then:
            assertThat(coverage.context, equalTo(Coverage.CONTEXT))
            assertThat(coverage.repository, equalTo(gitRepo))
            assertThat(coverage.gitToken, equalTo(gitToken))
            assertThat(coverage.sonarqubeClient, notNullValue())
            assertThat(coverage.sonarqubeClient.token, equalTo(token))
            assertThat(coverage.sonarqubeClient.hostname, equalTo(hostname as String))
    }

    def "Get a coverage metric for one project from Sonarqube"() {
        given:
            generateOKCoverageResponses("test:1", "metric-coverage-57_4-OK.json")

        when:
            coverage.retrieveCodeCoverageMetrics(["test:1"])

        then:
            [57.4]
    }

    def "Get coverage metrics for a project pair from Sonarqube"() {
        given:
            generateOKCoverageResponses("test:1", "metric-coverage-57_4-OK.json")
            generateOKCoverageResponses("test:2", "metric-coverage-48_92-OK.json")

        when:
            coverage.retrieveCodeCoverageMetrics(["test:1", "test:2"])

        then:
            [57.4, 48.92]
    }

    def "Raise An Exception if the data received from Sonarqube is Incorrect"() {
        given:
            generateOKCoverageResponses("test:2", "metric-coverage-48_92-DataFormatException.json")

        when:
            coverage.retrieveCodeCoverageMetrics(["test:2"])

        then:
            def e = thrown(DataIntegrityException)

            assertThat(e, notNullValue())
            assertThat(e.rawMessage, notNullValue())
    }

    def "Raise An Exception if the data received from Sonarqube has Null values"() {
        given:
            generateOKCoverageResponses("test:2", "metric-coverage-48_92-DataFormatException-NullValue.json")

        when:
            coverage.retrieveCodeCoverageMetrics(["test:2"])

        then:
            def e = thrown(DataIntegrityException)
            assertThat(e, notNullValue())
            assertThat(e.rawMessage, notNullValue())
    }

    def "Get new coverage and current coverage metric for a project from Sonarqube"() {
        given:
            generateOKCoverageResponses("test:1", "metric-coverage-57_4-71_4-OK.json", "coverage,new_coverage")

        when:
            coverage.retrieveCodeCoverageMetrics(["test:1"], true)

        then:
            [57.4, 71.44]
    }

    def "Get new coverage and current coverage metric for a pair of projects from Sonarqube"() {
        given:
            generateOKCoverageResponses("test:1", "metric-coverage-57_4-71_4-OK.json", "coverage,new_coverage")
            generateOKCoverageResponses("test:2", "metric-coverage-48_92-13_19-OK.json", "coverage,new_coverage")

        when:
            coverage.retrieveCodeCoverageMetrics(["test:1", "test:2"], true)

        then:
            [57.4, 71.44, 48.92, 13.2]
    }

    def "Raise An Exception if no project Ids were provided"() {
        given:
            generateOKCoverageResponses("test:1", "metric-coverage-57_4-OK.json")

        when:
            coverage.retrieveCodeCoverageMetrics([])

        then:
            def e = thrown(DataIntegrityException)

            assertThat(e, notNullValue())
            assertThat(e.rawMessage, notNullValue())
    }

    def "Compare Coverage Metric and update status for failure"() {
        given:
            def customCoverage = new CustomCoverageImpl(hostname, token, gitRepo, gitToken, hostname)
            def targetUrl = "${customCoverage.sonarqubeClient.hostname}/component_measures?id=${URLEncoder.encode("test:1", "UTF-8")}&metric=coverage"

            generateOKCoverageResponses("test:1", "metric-coverage-57_4-OK.json")
            generateOKCoverageResponses("test:2", "metric-coverage-48_92-OK.json")
            generateGithubResponse(prId, "github-status-request-failure-57_4-48_92.json", "github-status-response-failure-57_4-48_92.json", coverage.gitToken, 201, ["TARGET_URL", targetUrl])

        when:
            def result = customCoverage.compare(prId, ["test:1", "test:2"])
        then:
            notThrown(Exception)
            assertThat(result.state, equalTo("failure"))
            assertThat(result.context, equalTo(Coverage.CONTEXT))
            assertThat(result.target_url, equalTo(targetUrl as String))
            assertThat(result.description, equalTo("New code reduced the coverage from 57.4% to 48.92%"))
    }

    def "Compare Coverage Metric and update status for success"() {
        given:
            def customCoverage = new CustomCoverageImpl(hostname, token, gitRepo, gitToken, hostname)
            def targetUrl = "${customCoverage.sonarqubeClient.hostname}/component_measures?id=${URLEncoder.encode("test:2", "UTF-8")}&metric=coverage"

            generateOKCoverageResponses("test:1", "metric-coverage-57_4-OK.json")
            generateOKCoverageResponses("test:2", "metric-coverage-48_92-OK.json")
            generateGithubResponse(prId, "github-status-request-success-48_92-57_4.json", "github-status-response-success-48_92-57_4.json", coverage.gitToken, 201, ["TARGET_URL", targetUrl])

        when:
            def result = customCoverage.compare(prId, ["test:2", "test:1"])
        then:
            notThrown(Exception)
            assertThat(result.state, equalTo("success"))
            assertThat(result.context, equalTo(Coverage.CONTEXT))
            assertThat(result.target_url, equalTo(targetUrl as String))
            assertThat(result.description, equalTo("New code increased the coverage from 48.92% to 57.4%"))
    }

    def "Raise an Exception if the response HTTP status from Github is not between 200 and 299"() {
        given:
            def customCoverage = new CustomCoverageImpl(hostname, token, gitRepo, gitToken, hostname)
            def targetUrl = "${customCoverage.sonarqubeClient.hostname}/component_measures?id=${URLEncoder.encode("test:2", "UTF-8")}&metric=coverage"

            generateOKCoverageResponses("test:1", "metric-coverage-57_4-OK.json")
            generateOKCoverageResponses("test:2", "metric-coverage-48_92-OK.json")
            generateGithubResponse("123", "github-status-request-success-48_92-57_4.json", "github-status-response-success-48_92-57_4.json", coverage.gitToken, 201, ["TARGET_URL", targetUrl])

        when:
            customCoverage.compare(prId, ["test:2", "test:1"])

        then:
            def e = thrown(GithubException)
            assertThat(e.code, equalTo(404))
            assertThat(e.message, notNullValue())
            assertThat(e.body, notNullValue())
    }

    class CustomCoverageImpl extends Coverage {
        CustomCoverageImpl(String hostname, String token, String gitRepo, String gitToken, String gitHostname) {
            super(hostname, token, gitRepo, gitToken)
            this.gitHostname = gitHostname
        }
    }
}
