package com.travelstart.plugins

import org.mockserver.integration.ClientAndServer
import spock.lang.Shared
import spock.lang.Specification

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

}
