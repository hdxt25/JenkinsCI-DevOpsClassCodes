def call(){
  dependencyCheck additionalArguments: '--scan ./', odcInstallation: 'Dp-Check'
  dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
}