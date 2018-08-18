#!/usr/bin/env groovy
import com.travelstart.plugins.jenkins.sonar.Coverage

def call(final Map args) {
    //approvalPlugins()

    println(env.SONAR_HOST_URL)
    println(env.SONAR_AUTH_TOKEN)

    final def coverage = new Coverage(env.SONAR_HOST_URL, env.SONAR_AUTH_TOKEN)
    final def projects = []
    final def newMetric = args.new_coverage ? (args.new_coverage) as Boolean : false

    projects.add(args.originalId)
    projects.add(args.newId)
    //args.originalId ?: projects.add(args.originalId)
    //args.newId ?: projects.add(args.newId)

    println(args.originalId)
    println(args.newId)

    def coverageList = coverage.retrieveCodeCoverageMetrics(projects, newMetric)
    println("IT WORKS!!!!")
    println(coverageList)
    println("COVERAGE END")

}