#!/usr/bin/env groovy
import com.travelstart.plugins.jenkins.sonar.Coverage

ORIGINAL_ID = "originalId"
NEW_ID = "newId"
METRIC_NEW = "new_coverage"

def call(final Map args) {

    final def coverage = new Coverage(env.SONAR_HOST_URL, env.SONAR_AUTH_TOKEN)
    final def projects = []
    final def newMetric = args.containsKey(METRIC_NEW) && args.get(METRIC_NEW)? (args.get(METRIC_NEW)) as Boolean : false

    args.containsKey(ORIGINAL_ID) && args.get(ORIGINAL_ID)?: projects.add(args.get(ORIGINAL_ID))
    args.containsKey(NEW_ID) && args.get(NEW_ID)?: projects.add(args.get(NEW_ID))

    def coverageList = coverage.retrieveCodeCoverageMetrics(projects, newMetric)
    println(coverageList)
}