@Library('Shared') _
pipeline {
    agent { label "mynode" }

    tools {
        maven 'mvn'
    }

    environment {
        SONAR_HOME = tool 'sonar-scanner'
        NVD_API_KEY = credentials('NVD_API_KEY')
        AWS_ACCOUNT_ID = credentials('ACCOUNT_ID')
        AWS_ECR_REPO_NAME = credentials('ECR_REP4')
        AWS_DEFAULT_REGION = 'us-east-2'
        REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/"
        IMAGE_URI = "${REPOSITORY_URI}${AWS_ECR_REPO_NAME}:${BUILD_NUMBER}"
        GIT_REPO_NAME = "JenkinsCI-DevOpsClassCodes"
    }

    stages {
        stage("Workspace cleanup"){
            steps{
                script{
                    cleanWs()
                }
            }
        }
        
        // Using Shared Library vars folder for exeuting Jenkins CI Pipeline stages
        stage('Git: Code Checkout') {
            steps {
                script{
                    code_checkout("https://github.com/hdxt25/JenkinsCI-DevOpsClassCodes.git","main","GITHUB_CRED")
                }
            }
        }

        stage('Build,Compile,test,package') {
            steps {
                sh 'mvn compile'
                sh 'mvn -P metrics pmd:pmd'
                sh 'mvn test' 
                sh 'mvn package'
            }
        }

        stage('Trivy File Scan') {
            steps {
                script{
                    trivy_scan()
                }                
            }
        }

        stage('OWASP Dependency-Check Scan') {
            steps {
                script{
                    owasp_dependency()
                }               
            }
        }

        stage('SonarQube: Code Analysis') {
            steps {
                script {
                    mvn_sonarqube_analysis('sonar-server', 'devopsclasscodes', 'devopsclasscodes')
                }
            }
        }

        // stage('SonarQube: Code Quality Gates') {
        //     steps {
        //         script{
        //             dir('src'){
        //                sonarqube_code_quality()
        //             }
        //         }
        //     }
        // }
        
        // Multiarch Image build and push to DockerHub Repository
    
        stage("Docker Image multiarch build & push") {
            steps {
                script {
                        docker_multiarch_build("devopsclasscodes","${env.BUILD_NUMBER}","hdxt25")
                }
            }
        }

        stage("TRIVY Image Scan") {
            steps {
                script {
                        trivy_multiarch_image_scan("devopsclasscodes","${env.BUILD_NUMBER}","hdxt25")                   
                }
            }
        }

        stage("Update: Kubernetes manifest"){
            steps{
                script{
                    dir('kubernetes'){
                        sh """
                            BUILD_NUMBER=\$(git rev-parse HEAD)
                            echo "Current BUILD NUMBER: \$BUILD_NUMBER"
                            sed -i 's|hdxt25/devopsclasscodes:.*|hdxt25/devopsclasscodes:\${BUILD_NUMBER}|g' devopsclasscodes-deployment.yaml
                            grep -A 1 "image:" devopsclasscodes-deployment.yaml              
                        """
                    }
                }
            }
        }
        
        stage("Git: Code update and push to GitHub"){
            steps{
                script{
                    dir('kubernetes'){
                        withCredentials([usernamePassword(
                                credentialsId: 'GITHUB_CRED', 
                                usernameVariable: 'GITHUB_USER_NAME', 
                                passwordVariable: 'GITHUB_TOKEN')]) {
                            sh """
                                git config user.email "hdxt25@gmail.com"
                                git config user.name "himanshu"
                        
                    
                                echo "Adding changes to git: "
                                git add devopsclasscodes-deployment.yaml
                        
                                echo "Commiting changes: "
                                git commit -m "Updated K8s Deployment Docker Image Version ${BUILD_NUMBER}" || echo "No changes to commit"
                        
                                echo "Pushing changes to github: "
                                git push https://${GITHUB_TOKEN}@github.com/${GITHUB_USER_NAME}/${env.GIT_REPO_NAME} HEAD:main
                            """
                        }    
                    }
                }
            }
        }
    }
}
