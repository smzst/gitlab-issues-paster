import com.google.api.client.auth.oauth2.Credential
import com.typesafe.config.ConfigFactory
import gitlab.{GitLabService, GitLabServiceConfiguration}
import google.{GoogleService, GoogleServiceConfiguration}

import scala.jdk.CollectionConverters._

object Main extends App {
  val conf              = ConfigFactory.load()
  val googleServiceConf = GoogleServiceConfiguration(conf.getConfig("google"))
  val gitlabServiceConf = GitLabServiceConfiguration(conf.getConfig("gitlab"))

  val gitlabService = new GitLabService(
    gitlabServiceConf.privateToken,
    gitlabServiceConf.domainName,
    gitlabServiceConf.apiVersion,
    gitlabServiceConf.projectId
  )
  val googleService = new GoogleService(
    googleServiceConf.credentialsFilePath,
    googleServiceConf.scopes,
    googleServiceConf.credentialStoreDirectory,
    googleServiceConf.applicationName
  )

  val tickets            = gitlabService.fetch()
  val request            = new SheetsRequestCreator(tickets).create()
  val batchUpdateRequest = GoogleService.batchUpdateRequest(request.asJava)

  val credential: Credential = googleService.authorize()
  googleService
    .sheetsService(credential)
    .spreadsheets()
    .batchUpdate(googleServiceConf.spreadSheetId, batchUpdateRequest)
    .execute()
}
