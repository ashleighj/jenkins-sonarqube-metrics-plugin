package com.travelstart.plugins.jenkins.sonar

import org.junit.Assert
import org.junit.Test

class CoverageTest {

    @Test
    void givenAPairOfProjectIds_ItShould_GetMetricsFromSonarqube() {
        def coverage = new Coverage()
        Assert.assertArrayEquals((Double[]) [2.5], coverage.retrieveCodeCoverageMetrics(["test1:test"]))
    }
}
