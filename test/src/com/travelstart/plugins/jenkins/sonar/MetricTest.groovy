package com.travelstart.plugins.jenkins.sonar

import com.travelstart.plugins.BaseTest
import com.travelstart.plugins.exceptions.SonarqubeException

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock

class MetricTest extends BaseTest {

    def "Raise An Exception if HTTP response status code is not between 200 and 299"() {
        given:
            def response = mock(HttpURLConnection.class)

            doReturn(404).when(response).responseCode
            doReturn("Not Found").when(response).responseMessage
            doReturn(importFile("404.json").text).when(response).content

        when:
            Metric.isSuccessful(response)

        then:
            def e = thrown(SonarqubeException)
            def expectedResponse = importFile("404.json").text

            assertThat(e.code, equalTo(404))
            assertThat(e.message, equalTo("Not Found"))
            assertThat(e.body, equalTo(expectedResponse))
    }

    def "Continue without failure if HTTP response code is between 200 and 299"() {
        given:
            def response = mock(HttpURLConnection.class)

            doReturn(200).when(response).responseCode

        when:
            (200..300).each {
                Metric.isSuccessful(response)
            }

        then:
            notThrown(Exception)
    }
}

