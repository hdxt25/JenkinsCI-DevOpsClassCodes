// vars/ecr_image_push.groovy
def call(String AWS_ECR_REPO_NAME, String AWS_DEFAULT_REGION, String REPOSITORY_URI) {
    def buildNumber = env.BUILD_NUMBER

    sh """
        echo "Logging in to AWS ECR..."
        aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${REPOSITORY_URI}

        echo "Tagging image..."
        docker tag ${AWS_ECR_REPO_NAME} ${REPOSITORY_URI}${AWS_ECR_REPO_NAME}:${buildNumber}

        echo "Pushing image to ECR..."
        docker push ${REPOSITORY_URI}${AWS_ECR_REPO_NAME}:${buildNumber}

        echo "ECR push completed for build #${buildNumber}"
    """
}
