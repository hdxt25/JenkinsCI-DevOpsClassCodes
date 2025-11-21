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
        AWS_ECR_REPO_NAME = credentials('ECR_REPOS3')
        AWS_DEFAULT_REGION = 'us-east-2'
        REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/"
        IMAGE_URI = "${REPOSITORY_URI}${AWS_ECR_REPO_NAME}:${BUILD_NUMBER}"
        GIT_REPO_NAME = "JenkinsCI-2-Tier-SpringBoot-bankapp"
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

        stage('Build') {
            steps {
                sh 'mvn clean verify -DskipTests=true '  //skip tests as its 2 Tier app mysql not available.
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
                    mvn_sonarqube_analysis('sonar-server', 'bankapp', 'bankapp')
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
        
        // Image build and push to aws DockerHub Repository
        stage("Docker Image Build") {
            steps {
                script {
                        docker_build("bankapp","${env.BUILD_NUMBER}","hdxt25")            
                }
            }
        }

        stage("Docker Image Push") {
            steps {
                script {
                        docker_push("bankapp","${env.BUILD_NUMBER}","hdxt25")
                }
            }
        }

        stage("TRIVY Image Scan") {
            steps {
                script {
                        trivy_image_scan("bankapp","${env.BUILD_NUMBER}","hdxt25")                   
                }
            }
        }

        // Image build and push to aws ECR Repository
        stage("Ecr Image Build") {
            steps {
                script {
                        ecr_image_build(AWS_ECR_REPO_NAME)
                }
            }          
        }

        stage("Ecr Image Push") {
            steps {
                script {
                        ecr_image_push(AWS_ECR_REPO_NAME, AWS_DEFAULT_REGION, REPOSITORY_URI)                  
                }
            }
        }

        stage("TRIVY Docker Image Scan") {
            steps {
                script {
                    trivy_ecr_image_scan(IMAGE_URI)
                }
            }
        }    

        stage("Update: Kubernetes manifest"){
            steps{
                script{
                    dir('kubernetes/Bankapp'){
                        sh """
                           # GIT_COMMIT=\$(git rev-parse HEAD)
                           # echo "Current Git commit: \$GIT_COMMIT"
                           # sed -i 's|hdxt25/bankapp:.*|hdxt25/bankapp:\${GIT_COMMIT}|g' bankapp-deployment.yaml
                           # grep -A 1 "image:" bankapp-deployment.yaml

                            #for ECR image update in bankapp-deployment.yaml
                            NEW_IMAGE="${REPOSITORY_URI}${AWS_ECR_REPO_NAME}:${BUILD_NUMBER}"
                            echo "Updating deployment.yaml to use image: \${NEW_IMAGE}"

                            # set in deployment.yaml -> image: 839087537051.dkr.ecr.us-east-2.amazonaws.com/frontend:0
                            # Update deployment.yaml with the new image
                            sed -i "s|image: ${REPOSITORY_URI}${AWS_ECR_REPO_NAME}:.*|image: \${NEW_IMAGE}|g" bankapp-deployment.yaml  
                            # Show the updated line
                            grep -A 1 "image:" bankapp-deployment.yaml
                        """
                    }
                }
            }
        }
        
        stage("Git: Code update and push to GitHub"){
            steps{
                script{
                    dir('kubernetes/Bankapp'){
                        withCredentials([usernamePassword(
                                credentialsId: 'GITHUB_CRED', 
                                usernameVariable: 'GITHUB_USER_NAME', 
                                passwordVariable: 'GITHUB_TOKEN')]) {
                            sh """
                                git config user.email "hdxt25@gmail.com"
                                git config user.name "himanshu"
                        
                    
                                echo "Adding changes to git: "
                                git add bankapp-deployment.yaml
                        
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
