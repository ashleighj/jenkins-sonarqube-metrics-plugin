package com.travelstart.plugins.utils

import com.travelstart.plugins.BaseTest
import groovy.json.internal.JsonParserCharArray
import spock.lang.Shared

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.notNullValue

class RestClientTest extends BaseTest {

    @Shared def client = new RestClient(hostname: "https://jsonplaceholder.typicode.com")

    def "Return an OK HTTP response for a correct HTTPRequest without URL params"() {
        given:
            def endpoint = "/posts/1"
            def parser = new JsonParserCharArray()

        when:
            def response = client.get(endpoint)

        then:
            assertThat(response, notNullValue())
            assertThat(response.getURL().toString(), equalTo("https://jsonplaceholder.typicode.com/posts/1"))
            assertThat(response.content, notNullValue())
            assertThat(response.responseCode, equalTo(200))

            def body = parser.parse(response.content as InputStream)

            assertThat(body?.userId, equalTo(1))
            assertThat(body?.title, equalTo("sunt aut facere repellat provident occaecati excepturi optio reprehenderit"))
            assertThat(body?.body, equalTo("quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"))
    }

    def "Return an OK HTTP response for a correct HTTPRequest with URL params"() {
        given:
            def endpoint = "/comments"
            def params = [postId: "1"]
            def parser = new JsonParserCharArray()

        when:
            def response = client.get(endpoint, params)

        then:
            assertThat(response, notNullValue())
            assertThat(response.getURL().toString(), equalTo("https://jsonplaceholder.typicode.com/comments?postId=1"))
            assertThat(response.content, notNullValue())
            assertThat(response.responseCode, equalTo(200))

            def body = parser.parse(response.content as InputStream)

            assertThat(body, instanceOf(List))
            assertThat(body?.get(0)?.id, equalTo(1))
            assertThat(body?.get(0)?.name, equalTo("id labore ex et quam laborum"))
            assertThat(body?.get(0)?.email, equalTo("Eliseo@gardner.biz"))
    }

    def "Return an OK HTTP response for a correct HTTPRequest with URL params and Security Headers"() {
        given:
            def endpoint = "/todos/1"
            def params = [filter1: "val1", filter2: "val2"]

            client.token = "1234567890"

        when:
            def response = client.get(endpoint, params)

        then:
            assertThat(response, notNullValue())
            assertThat(response.getURL().toString(), equalTo("https://jsonplaceholder.typicode.com/todos/1?filter1=val1&filter2=val2"))
            assertThat(response.content, notNullValue())
            assertThat(response.responseCode, equalTo(200))
    }
}
