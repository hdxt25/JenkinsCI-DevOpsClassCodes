def call(String sonarServer, String projectName, String projectKey) {
    withSonarQubeEnv(sonarServer) {
        sh """
            mvn sonar:sonar \
              -Dsonar.projectKey=${projectKey} \
              -Dsonar.projectName=${projectName} \
              -Dsonar.host.url=$SONAR_HOST_URL \
              -Dsonar.token=$SONAR_AUTH_TOKEN
        """
    }
}
