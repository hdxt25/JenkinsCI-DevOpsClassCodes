def call(String GitUrl, String GitBranch, String GithubToken){
  git url: "${GitUrl}", branch: "${GitBranch}", credentialsId: "${GithubToken}"
}
