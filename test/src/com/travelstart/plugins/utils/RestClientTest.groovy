package com.travelstart.plugins.utils

import com.travelstart.plugins.BaseTest
import groovy.json.JsonSlurper
import org.mockserver.model.Parameter
import spock.lang.Shared

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.notNullValue
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

class RestClientTest extends BaseTest {
    def client

    @Shared def parser = new JsonSlurper()

    def setup() {
        setupServer()
        client = new RestClient(hostname: hostname)
    }

    void cleanup() {
        cleanupServer()
        client = null
    }

    void setupHttpResponseWithoutSecurity(final String endpoint, final String path,
                                          final Map<String, String> params = [:], final String method = "GET") {
        final def body = importFile(path).text
        final def urlParams = []
        params.each { urlParams.add(new Parameter(it.key, it.value)) }

        //http://localhost:{port}/{endpoint}?{urlParams}
        mockServer.when(
                request()
                        .withMethod(method)
                        .withPath(endpoint)
                        .withQueryStringParameters(urlParams))
                .respond(
                response()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatusCode(200)
        )

    }

    void setupHttpResponseWithSecurity(final String endpoint, final String path, final Map<String, String> params = [:],
                                       final String expectedAuth = "", final String method = "GET") {
        final def body = importFile(path).text
        final def urlParams = []
        params.each { urlParams.add(new Parameter(it.key, it.value)) }

        //http://localhost:{port}/{endpoint}?{urlParams}
        mockServer.when(
                request()
                        .withMethod(method)
                        .withPath(endpoint)
                        .withQueryStringParameters(urlParams)
                        .withHeader("Authorization", "Basic ${expectedAuth}"))
                .respond(
                response()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatusCode(200)
        )

    }

    void setupHttpResponseWithSecurityAndPayload(final String endpoint, final String path, final String requestPath,
                                                final Map<String, String> params = [:], final String expectedAuth = "",
                                                final String method = "GET") {
        final def requestBody = importFile(requestPath).text
        final def body = importFile(path).text
        final def urlParams = []
        params.each { urlParams.add(new Parameter(it.key, it.value)) }

        //http://localhost:{port}/{endpoint}?{urlParams}
        mockServer.when(
                request()
                        .withMethod(method)
                        .withPath(endpoint)
                        .withQueryStringParameters(urlParams)
                        .withBody(requestBody)
                        .withHeader("Authorization", "Basic ${expectedAuth}"))
                .respond(
                response()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatusCode(200)
        )

    }

    def "Return an OK HTTP response for a correct HTTPRequest without URL params"() {
        given:
            def endpoint = "/posts/1"
            setupHttpResponseWithoutSecurity(endpoint, "post-1.json")

        when:
            def response = client.get(endpoint)

        then:
            assertThat(response, notNullValue())
            assertThat(response.getURL().toString(), equalTo("${hostname}${endpoint}" as String))
            assertThat(response.content, notNullValue())
            assertThat(response.responseCode, equalTo(200))

            def body = parser.parseText(response.content.text as String)

            assertThat(body?.userId, equalTo(1))
            assertThat(body?.title, equalTo("sunt aut facere repellat provident occaecati excepturi optio reprehenderit"))
            assertThat(body?.body, equalTo("quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"))
    }

    def "Return an OK HTTP response for a correct HTTPRequest with URL params"() {
        given:
            def endpoint = "/comments"
            def params = [postId: "1"]
            setupHttpResponseWithoutSecurity(endpoint, "postsId-1.json", params)

        when:
            def response = client.get(endpoint, params)

        then:
            assertThat(response, notNullValue())
            assertThat(response.getURL().toString(), equalTo("${hostname}${endpoint}?postId=1" as String))
            assertThat(response.content, notNullValue())
            assertThat(response.responseCode, equalTo(200))

            def body = parser.parseText(response.content.text as String)

            assertThat(body, instanceOf(List))
            assertThat(body?.get(0)?.id, equalTo(1))
            assertThat(body?.get(0)?.name, equalTo("id labore ex et quam laborum"))
            assertThat(body?.get(0)?.email, equalTo("Eliseo@gardner.biz"))
    }

    def "Return an OK HTTP response for a correct HTTPRequest with URL params and Security Headers"() {
        given:
            def endpoint = "/todos/1"
            def params = [filter1: "val1", filter2: "val2"]
            setupHttpResponseWithSecurity(endpoint, "todos-1.json", params, "MTIzNDU2Nzg5MDo=")

            client.token = "1234567890"

        when:
            def response = client.get(endpoint, params)

        then:
            assertThat(response, notNullValue())
            assertThat(response.getURL().toString(), equalTo("${hostname}${endpoint}?filter1=val1&filter2=val2" as String))
            assertThat(response.content, notNullValue())
            assertThat(response.responseCode, equalTo(200))
    }

    def "Return an OK HTTP response for a correct POST HTTPRequest with URL params"() {
        given:
            def endpoint = "/posts/1"
            def params = [filter1: "val1", filter2: "val2"]
            setupHttpResponseWithSecurityAndPayload(endpoint, "post-1.json", "post-1.json", params,
                "MTIzNDU2Nzg5MDo=", "POST")

            client.token = "1234567890"

        when:
            def response = client.post(endpoint, params, importFile("post-1.json").text)

        then:
            assertThat(response, notNullValue())
            assertThat(response.getURL().toString(), equalTo("${hostname}${endpoint}?filter1=val1&filter2=val2" as String))
            assertThat(response.content, notNullValue())
            assertThat(response.responseCode, equalTo(200))
    }
}
