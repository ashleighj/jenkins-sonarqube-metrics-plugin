package com.travelstart.plugins.traits

import com.travelstart.plugins.BaseTest
import com.travelstart.plugins.exceptions.SonarqubeException
import org.junit.Test

import static junit.framework.TestCase.assertEquals
import static junit.framework.TestCase.fail
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock

class TraitTest extends BaseTest implements HandleException {

    @Test
    void givenAnHttpResponse_OnErrorCode_ItShouldRaiseAnException() {
        final def response = mock(HttpURLConnection.class)
        doReturn(404).when(response).responseCode
        doReturn("Not Found").when(response).responseMessage
        doReturn(importFile("404.json").text).when(response).content

        try {
            isSuccessful(response)
            fail()
        } catch (SonarqubeException e) {
            assertEquals(404, e.code)
            assertEquals("Not Found", e.message)
            assertEquals(importFile("404.json").text, e.body)
            assert true
        } catch (Exception e) {
            e.printStackTrace()
            fail("Unexpected Exception: It should handle any HTTP error from the server")
        }
    }

    @Test
    void givenAnHttpResponse_OnSuccess_ItShouldReturnTrue() {
        final def response = mock(HttpURLConnection.class)
        doReturn(200).when(response).responseCode

        (200..300).each {
            isSuccessful(response)
            assert true
        }
    }
}

