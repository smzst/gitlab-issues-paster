import java.io.{InputStream, InputStreamReader}
import java.time.format.DateTimeFormatter
import java.util

import SheetsWrapper._
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
    gitlabServiceConf.projectId
  )

  val header = generateRow(
    Seq("ID", "State", "Title", "Description", "Created at", "Updated at", "Labels", "Assignees", "Author", "Weight")
      .map(generateStringCell)
  )
  val body: Seq[Seq[RowData]] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    val tickets   = gitlabService.fetch()
    gitlabService
      .extractBackLogTickets(tickets)
      .value
      .map(
        t =>
          generateRow(
            Seq(
              generateFormulaCell(s"""=hyperlink("${t.url}", "${t.id.toString}")"""),
              generateStringCell(t.state.value),
              generateStringCell(t.title),
              generateStringCell(t.description),
              generateStringCell(formatter.format(t.createdAt)),
              generateStringCell(formatter.format(t.updatedAt)),
              generateStringCell(t.labels.mkString(", ")),
              generateStringCell(t.assignees.mkString(", ")),
              generateStringCell(t.author),
              generateNumberCell(t.weight)
            )
          )
      )
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
