#!/usr/bin/env groovy
//import com.travelstart.plugins.jenkins.sonar.Coverage

def call(final Map args) {

    //final def coverage = new Coverage(env.SONAR_HOST_URL, env.SONAR_AUTH_TOKEN)
    final def coverage = new com.travelstart.plugins.jenkins.sonar.Coverage("url", "token")
    final def projects = []
    final def newMetric = args.new_coverage ? (args.new_coverage) as Boolean : false

    args.originalId ?: projects.add(args.originalId)
    args.newId ?: projects.add(args.originalId)

    //def coverageList = coverage.retrieveCodeCoverageMetrics(projects, newMetric)
    println("IT WORKS!!!!")
    //println(coverageList)
}