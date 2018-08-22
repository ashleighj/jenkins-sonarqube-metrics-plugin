# Jenkins Sonarque Metrics Library/Plugin

It uses Sonarqube to get metrics during a Pull Request and create additional checks in the PR based on custom thresholds.


Coding tips

Don't use traits: Traits are awesome and they help you with reusability. However, 
jenkins pipeline doesn't support traits. Generating a compilation error

https://issues.jenkins-ci.org/browse/JENKINS-46145