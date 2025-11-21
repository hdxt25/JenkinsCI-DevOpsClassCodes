
def call(String IMAGE_URI) {
    sh "trivy image ${IMAGE_URI} > trivyimage.txt"
}
