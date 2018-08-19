package com.travelstart.plugins

import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Parameter
import spock.lang.Shared
import spock.lang.Specification

import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

abstract class BaseTest extends Specification {
    def hostname
    def port
    def mockServer

    @Shared def random = new Random()

    final String packagePath = "test/resources/${getClass().getPackage().getName().replace(".", "/")}"

    def importFile(final String path) {
        final def uri = new File("${packagePath}/${path}").toURI()
        return new File(uri)
    }


    def setupServer() {
        port = random.nextInt(6000) + 2000
        hostname = "http://localhost:${port}"
        mockServer = ClientAndServer.startClientAndServer(port)
    }

    void cleanupServer() {
        hostname = null
        port = 0

        if (mockServer != null) {
            mockServer.stop()
            mockServer = null
        }
    }

    void generateGithubResponse(final String prId, final String reqPath, final String resPath,
                                final String accessToken, final int statusCode = 201) {
        final def urlParams = [new Parameter("access_token", accessToken)]
        final def resBody = importFile(resPath).text
        final def reqBody = importFile(reqPath).text

        //https://localhost:{port}/repos/mock/repo/{prId}?{urlParams}
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/mock/repo/${prId}")
                        .withBody(reqBody)
                        .withQueryStringParameters(urlParams)
                        .withHeader("Content-Type", "application/json"))
                .respond(
                response()
                        .withHeader("Content-Type", "application/json")
                        .withBody(resBody)
                        .withStatusCode(statusCode)
        )
    }

}
