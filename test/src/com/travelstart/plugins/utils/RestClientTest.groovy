package com.travelstart.plugins.utils

import groovy.json.internal.JsonParserCharArray
import org.junit.Assert
import org.junit.Test

class RestClientTest {

    @Test
    void GivenAGetRequest_WhenItsCorrectAndNoParams_ItShouldReturnAnOKResponse() {
        final def client = new RestClient(hostname: "https://jsonplaceholder.typicode.com")
        final def endpoint = "/posts/1"
        final def response = client.get(endpoint)
        final def parser = new JsonParserCharArray()

        Assert.assertNotNull(response)
        Assert.assertEquals("https://jsonplaceholder.typicode.com/posts/1", response.getURL().toString())
        Assert.assertNotNull(response.content)
        Assert.assertEquals(200, response.responseCode)

        final def body = parser.parse(response.content as InputStream)

        Assert.assertEquals(1, body.userId)
        Assert.assertEquals("sunt aut facere repellat provident occaecati excepturi optio reprehenderit", body.title)
        Assert.assertEquals("quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto", body.body)

    }

    @Test
    void GivenAGetRequest_WhenItsCorrectAndWithParams_ItShouldReturnAnOKResponse() {
        final def client = new RestClient(hostname: "https://jsonplaceholder.typicode.com")
        final def endpoint = "/comments"
        final def params = [postId: "1"]
        final def response = client.get(endpoint, params)
        final def parser = new JsonParserCharArray()

        Assert.assertNotNull(response)
        Assert.assertEquals("https://jsonplaceholder.typicode.com/comments?postId=1", response.getURL().toString())
        Assert.assertNotNull(response.content)
        Assert.assertEquals(200, response.responseCode)

        final def body = parser.parse(response.content as InputStream)

        Assert.assertTrue(body instanceof List)
        Assert.assertFalse((body as List).isEmpty())

        Assert.assertNotNull(body.get(0))
        Assert.assertEquals(1, body.get(0).id)
        Assert.assertEquals("id labore ex et quam laborum", body.get(0).name)
        Assert.assertEquals("Eliseo@gardner.biz", body.get(0).email)
    }

    @Test
    void GivenAGetRequest_WhenItsCorrectWithParamsAndSecurityHeaders_ItShouldReturnAnOKResponse() {
        final def client = new RestClient(hostname: "https://jsonplaceholder.typicode.com", token: "1234567890")
        final def endpoint = "/todos/1"
        final def params = [filter1: "val1", filter2: "val2"]
        final def response = client.get(endpoint, params)

        Assert.assertNotNull(response)
        Assert.assertEquals("https://jsonplaceholder.typicode.com/todos/1?filter1=val1&filter2=val2",
                response.getURL().toString())
        Assert.assertNotNull(response.content)
        Assert.assertEquals(200, response.responseCode)

    }
}
