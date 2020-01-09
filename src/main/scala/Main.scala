import java.io.{InputStream, InputStreamReader}
import java.time.format.DateTimeFormatter

import SheetsWrapper._
import com.google.api.client.auth.oauth2.Credential
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

  val tickets = gitlabService.extractBackLogTickets(gitlabService.fetch())

  val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

  val request = new RequestBuilder()
    .set("ID", tickets.value.map(t => generateFormulaCell(s"""=hyperlink("${t.url}", "${t.id.toString}")""")))
    .set("State", tickets.value.map(t => generateStringCell(t.state.value)))
    .set("Title", tickets.value.map(t => generateStringCell(t.title)))
    .set("Description", tickets.value.map(t => generateStringCell(t.description)))
    .set("Created_at", tickets.value.map(t => generateStringCell(formatter.format(t.createdAt))))
    .set("Updated_at", tickets.value.map(t => generateStringCell(formatter.format(t.updatedAt))))
    .set("Labels", tickets.value.map(t => generateStringCell(t.labels.mkString(", "))))
    .set("Assignees", tickets.value.map(t => generateStringCell(t.assignees.mkString(", "))))
    .set("Author", tickets.value.map(t => generateStringCell(t.author)))
    .set("Weight", tickets.value.map(t => generateNumberCell(t.weight)))
    .build()
    .asJava

  val batchUpdateRequest = GoogleService.batchUpdateRequest(request)

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
