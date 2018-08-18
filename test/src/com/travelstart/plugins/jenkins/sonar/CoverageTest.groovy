package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.BaseTest
import com.travelstart.plugins.exceptions.DataIntegrityException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Parameter

import static junit.framework.Assert.assertNotNull
import static junit.framework.Assert.fail
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

class CoverageTest extends BaseTest {
    String hostname
    int port
    ClientAndServer mockServer


    final def random = new Random()
    final def token = "1234567890"

    @Before
    void buildScenario() {
        port = random.nextInt(6000) + 2000
        hostname = "http://localhost:${port}"

        setupMockServer()
    }

    void setupMockServer() {
        mockServer = ClientAndServer.startClientAndServer(port)
    }


    @After
    void destroyScenario() {
        hostname = null
        port = 0

        stopMockServer()
    }

    void stopMockServer() {
        if (mockServer != null) {
            mockServer.stop()
        }
    }

    void generateOKCoverageResponses(final String component, final String path, final String metrics) {
        final def urlParams = [new Parameter("component", component), new Parameter("metricKeys", metrics)]
        final def file = importFile(path)

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
                    .withBody(file.text)
                    .withStatusCode(200)
        )
    }

    void generateOKCoverageResponses(final String component, final String path) {
        generateOKCoverageResponses(component, path, "coverage")
    }

    @Test
    void givenAProjectId_ThatExists_ItShould_GetMetricsFromSonarqube() {
        generateOKCoverageResponses("test:1", "metric-coverage-57_4-OK.json")

        final def coverage = new Coverage(hostname, token)
        Assert.assertArrayEquals((Double[]) [57.4], coverage.retrieveCodeCoverageMetrics(["test:1"]))
    }

    @Test
    void givenAPairOfProjectsIds_ThatBothExists_ItShould_GetMetricsFromSonarqube() {
        generateOKCoverageResponses("test:1", "metric-coverage-57_4-OK.json")
        generateOKCoverageResponses("test:2", "metric-coverage-48_92-OK.json")

        final def coverage = new Coverage(hostname, token)
        Assert.assertArrayEquals((Double[]) [57.4, 48.92], coverage.retrieveCodeCoverageMetrics(["test:1", "test:2"]))
    }

    @Test
    void givenAProjectId_WhichDataIsIncorrect_ItShould_RaiseADataIntegritytException() {
        generateOKCoverageResponses("test:2", "metric-coverage-48_92-DataFormatException.json")

        final def coverage = new Coverage(hostname, token)
        try {
            coverage.retrieveCodeCoverageMetrics(["test:2"])
            assert false

        } catch (DataIntegrityException e) {
            assertNotNull(e.getMessage())
            assertNotNull(e.rawMessage)
        } catch (Exception e) {
            e.printStackTrace()
            fail("It should raise a DataFormatException not an non-handled exception")
        }
    }

    @Test
    void givenAProjectId_WhichDataHasNullValues_ItShould_RaiseADataIntegritytException() {
        generateOKCoverageResponses("test:2", "metric-coverage-48_92-DataFormatException-NullValue.json")

        final def coverage = new Coverage(hostname, token)
        try {
            coverage.retrieveCodeCoverageMetrics(["test:2"])
            assert false

        } catch (DataIntegrityException e) {
            assertNotNull(e.getMessage())
            assertNotNull(e.rawMessage)
        } catch (Exception e) {
            e.printStackTrace()
            fail("It should raise a DataFormatException not an non-handled exception")
        }
    }

    @Test
    void givenAProjectId_ThatExists_OnNewFlag_ItShould_GetMetricsFromSonarqube() {
        generateOKCoverageResponses("test:1", "metric-coverage-57_4-71_4-OK.json", "coverage,new_coverage")

        final def coverage = new Coverage(hostname, token)
        Assert.assertArrayEquals((Double[]) [57.4, 71.44], coverage.retrieveCodeCoverageMetrics(["test:1"], true))
    }

    @Test
    void givenAPairOfProjectsIds_ThatBothExists_OnNewFlag_ItShould_GetMetricsFromSonarqube() {
        generateOKCoverageResponses("test:1", "metric-coverage-57_4-71_4-OK.json", "coverage,new_coverage")
        generateOKCoverageResponses("test:2", "metric-coverage-48_92-13_19-OK.json", "coverage,new_coverage")

        final def coverage = new Coverage(hostname, token)
        Assert.assertArrayEquals((Double[]) [57.4, 71.44, 48.92, 13.2], coverage.retrieveCodeCoverageMetrics(["test:1", "test:2"], true))
    }
}
