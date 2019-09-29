import com.typesafe.config.Config

case class GitLabServiceConfiguration(
    apiVersion: String,
    privateToken: String,
    domainName: String,
    groupId: String
)

object GitLabServiceConfiguration {
  def apply(config: Config): GitLabServiceConfiguration = new GitLabServiceConfiguration(
    config.getString("api-version"),
    config.getString("private-token"),
    config.getString("domain-name"),
    config.getString("group-id")
  )
}
