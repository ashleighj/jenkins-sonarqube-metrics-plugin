package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.BaseTest
import com.travelstart.plugins.exceptions.DataIntegrityException
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Parameter
import spock.lang.*

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*


import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

class CoverageTest extends BaseTest {
    String hostname
    int port
    ClientAndServer mockServer
    Coverage coverage

    @Shared def random = new Random()
    @Shared def token = "1234567890"

    def setup() {
        port = random.nextInt(6000) + 2000
        hostname = "http://localhost:${port}"
        mockServer = ClientAndServer.startClientAndServer(port)
        coverage = new Coverage(hostname, token)
    }

    void cleanup() {
        hostname = null
        port = 0

        if (mockServer != null) {
            mockServer.stop()
            mockServer = null
        }
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
}
