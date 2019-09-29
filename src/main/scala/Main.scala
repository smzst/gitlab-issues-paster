import java.io.{InputStream, InputStreamReader}
import java.util

import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.sheets.v4.model._
import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters._

object Main extends App {
  val conf              = ConfigFactory.load()
  val googleServiceConf = GoogleServiceConfiguration(conf.getConfig("google"))
  val gitlabServiceConf = GitLabServiceConfiguration(conf.getConfig("gitlab"))

  val gitlabService = new GitLabService(
    gitlabServiceConf.privateToken,
    gitlabServiceConf.domainName,
    gitlabServiceConf.apiVersion,
    gitlabServiceConf.groupId
  )

  val header = SheetsWrapper.generateRow(Seq("ID", "Title", "Weight", "Labels"))
  val body: Seq[Seq[RowData]] = {
    val tickets = gitlabService.fetch()
    gitlabService
      .extractBackLogTickets(tickets)
      .toSeq
      .map(t => SheetsWrapper.generateRow(Seq(t.getHyperLink, t.title, t.weight, t.labels)))
  }
  val rows: util.List[RowData] = (header ++ body.flatten).asJava
  val batchUpdateRequest       = GoogleService.batchUpdateRequest(rows)

  // `classOf[Nothing] getResourceAsStream CredentialsFilePath` と書いて sbt run するとぬるぽ発生した
  val inputStream: InputStream = getClass.getResourceAsStream(googleServiceConf.credentialsFilePath)

  val googleService =
    new GoogleService(
      new InputStreamReader(inputStream),
      googleServiceConf.scopes,
      googleServiceConf.credentialStoreDirectory,
      googleServiceConf.applicationName
    )
  val credential: Credential = googleService.authorize()

  googleService
    .sheetsService(credential)
    .spreadsheets()
    .batchUpdate(googleServiceConf.spreadSheetId, batchUpdateRequest)
    .execute()
}
