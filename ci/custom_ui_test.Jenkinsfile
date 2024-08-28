#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/ui_local_ruleset.yaml"

stage('UI Test') {
     script {
        ansiColor('xterm') {
            withSonarQubeEnv("${env.SQ_SERVER}") {
                sh "${bob} -r ${ruleset} test"
            }
        }
    }
}