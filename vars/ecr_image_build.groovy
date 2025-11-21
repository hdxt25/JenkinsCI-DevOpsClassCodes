// vars/docker_build.groovy
def call(String AWS_ECR_REPO_NAME) {
    sh 'docker system prune -f'
    sh 'docker container prune -f'

    sh """
        echo "Building Docker image: ${AWS_ECR_REPO_NAME}"
        docker build -t ${AWS_ECR_REPO_NAME} .
    """
}
