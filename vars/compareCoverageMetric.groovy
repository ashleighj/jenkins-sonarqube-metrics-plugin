#!/usr/bin/env groovy
import com.travelstart.plugins.jenkins.sonar.Coverage
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

def call(final Map args) {

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
    println(projects)

    def coverageList = coverage.retrieveCodeCoverageMetrics(projects, newMetric)
    println("IT WORKS!!!!")
    println(coverageList)

}

def approvalPlugins() {
    println("INFO: Whitelisting requirements for Jenkinsfile API Calls")

// Create a list of the required signatures
    def requiredSigs = [
            'method java.net.HttpURLConnection setRequestMethod java.lang.String',
            'method java.net.URL openConnection',
            'method java.net.URLConnection connect',
            'method java.net.URLConnection getContent',
            'method java.net.URLConnection getOutputStream',
            'method java.net.URLConnection setDoOutput boolean',
            'method java.net.URLConnection setRequestProperty java.lang.String java.lang.String',

            // Signatures already approved which may have introduced a security vulnerability (recommend clearing):
            'method java.net.URL openConnection',
    ]

    // Get a handle on our approval object
    approver = ScriptApproval.get()

    // Approve each of them
    requiredSigs.each {
        approver.approveSignature(it)
    }

    println("INFO: Jenkinsfile API calls signatures approved")
}