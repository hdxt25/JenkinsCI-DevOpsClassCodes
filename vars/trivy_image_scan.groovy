// vars/trivy_image_scan.groovy
def call(String projectName, String imageTag, String dockerhubuser) {
    // Use withCredentials safely for Docker login
    withCredentials([usernamePassword(
        credentialsId: 'docker', 
        usernameVariable: 'DOCKER_USER', 
        passwordVariable: 'DOCKER_PASS'
    )]) {
        // Build the full Docker image name
        def IMAGE = "${dockerhubuser}/${projectName}:${imageTag}"

        // Run Trivy scan
        sh """
            echo "Logging into Docker Hub..."
            echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
            echo "Scanning Docker image: ${IMAGE}"
            trivy image ${IMAGE} > trivyimage.txt
        """
    }
}


