def call(){
  sh "trivy fs --format json --output trivy-frontend-report.json ."
}
