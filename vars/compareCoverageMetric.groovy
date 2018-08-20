#!/usr/bin/env groovy
import com.travelstart.plugins.exceptions.PluginException
import com.travelstart.plugins.jenkins.sonar.Coverage

def call(final Map args) {

    echo "${args}"

    withCredentials([string(credentialsId: 'github-sonarqube-oauth', variable: 'GIT_SONAR_TOKEN_LOCAL')]) {
        final String gitToken = env.GIT_SONAR_TOKEN ?: "${GIT_SONAR_TOKEN_LOCAL}"

        final String gitRepo = env.GIT_REPO ?: sh(returnStdout: true, script: 'git config remote.origin.url').trim().replace("https://github.com","").replace(".git", "")
        final String gitPrId = env.GIT_COMMIT ?: sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
        final String sonarToken = env.SONAR_AUTH_TOKEN ?: "${env.SONAR_LOGIN}:${env.SONAR_PASSWORD}"

        final def coverage = new Coverage(env.SONAR_HOST_URL, sonarToken, gitRepo, gitToken)
        final def projects = []
        final def newMetric = args.new_coverage ? (args.new_coverage) as Boolean : false

        if (args.originalId)
            projects.add(args.originalId as String)

        if (args.originalId)
            projects.add(args.newId as String)

        try {
            coverage.compare(gitPrId, projects, newMetric)
            echo "Coverage Metric FINISHED! GitRepo: ${gitRepo}, GitPullRequestId: ${gitPrId}, SonarHost: ${env.SONAR_HOST_URL}, Projects: ${projects}"
        } catch (PluginException e) {
            echo "Coverage Metric FAILED! GitRepo: ${gitRepo}, GitPullRequestId: ${gitPrId}, SonarHost: ${env.SONAR_HOST_URL}, Projects: ${projects}"
            echo "Error Code: ${e.code}, Error message: ${e.message}, Error Body: ${e.body}"
            throw e
        }
    }

}