package com.travelstart.plugins.utils

import org.junit.Assert
import org.junit.Test

class RestClientTest {

    @Test
    void GivenAGetRequest_WhenItsCorrectAndNoParams_ItShouldReturnAnOKResponse() {
        def client = new RestClient(hostname: "https://jsonplaceholder.typicode.com")
        def endpoint = "/todos/1"

        Assert.assertNotNull(client.get(endpoint))
        Assert.assertEquals("https://jsonplaceholder.typicode.com/todos/1", client.get(endpoint).getURL().toString())
        Assert.assertNotNull(client.get(endpoint).content)
        Assert.assertEquals(200, client.get(endpoint).responseCode)
    }

    @Test
    void GivenAGetRequest_WhenItsCorrectAndWithParams_ItShouldReturnAnOKResponse() {
        def client = new RestClient(hostname: "https://jsonplaceholder.typicode.com")
        def endpoint = "/todos/1"
        def params = [filter1: "val1", filter2: "val2"]

        Assert.assertNotNull(client.get(endpoint))
        Assert.assertEquals("https://jsonplaceholder.typicode.com/todos/1?filter1=val1&filter2=val2",
                client.get(endpoint, params).getURL().toString())
        Assert.assertNotNull(client.get(endpoint).content)
        Assert.assertEquals(200, client.get(endpoint).responseCode)

    }

    @Test
    void GivenAGetRequest_WhenItsCorrectWithParamsAndSecurityHeaders_ItShouldReturnAnOKResponse() {
        def client = new RestClient(hostname: "https://jsonplaceholder.typicode.com", token: "1234567890")
        def endpoint = "/todos/1"
        def params = [filter1: "val1", filter2: "val2"]

        Assert.assertNotNull(client.get(endpoint))
        Assert.assertEquals("https://jsonplaceholder.typicode.com/todos/1?filter1=val1&filter2=val2",
                client.get(endpoint, params).getURL().toString())
        Assert.assertNotNull(client.get(endpoint).content)
        Assert.assertEquals(200, client.get(endpoint).responseCode)
        Assert.assertNotNull(client.get(endpoint).requestProperties.get("Authorisation").get(0))
        Assert.assertEquals("Basic MTIzNDU2Nzg5MDo=", client.get(endpoint).requestProperties.get("Authorisation").get(0))
    }
}
