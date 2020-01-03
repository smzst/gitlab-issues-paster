package google

import java.io.{File, InputStream, InputStreamReader}
import java.util

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model._

import scala.jdk.CollectionConverters._

class GoogleService(
    credentialsFilePath: String,
    authorizationScopes: Seq[String],
    credentialStoreDirectory: String,
    applicationName: String
) {

  private val HttpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val JsonFactory: JsonFactory        = JacksonFactory.getDefaultInstance

  def authorize(): Credential = {
    val inputStream: InputStream           = getClass.getResourceAsStream(credentialsFilePath)
    val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(JsonFactory, new InputStreamReader(inputStream))

    val dataStoreFactory: FileDataStoreFactory = new FileDataStoreFactory(new File(credentialStoreDirectory))

    val builder: GoogleAuthorizationCodeFlow.Builder = new GoogleAuthorizationCodeFlow.Builder(
      HttpTransport,
      JsonFactory,
      clientSecrets,
      authorizationScopes.asJava
    )

    builder.setDataStoreFactory(dataStoreFactory).setAccessType("offline")

    val flow: GoogleAuthorizationCodeFlow = builder.build()
    val receiver: LocalServerReceiver     = new LocalServerReceiver.Builder().setPort(8888).build()
    new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
  }

  def sheetsService(credential: Credential): Sheets =
    new Sheets.Builder(HttpTransport, JsonFactory, credential)
      .setApplicationName(applicationName)
      .build()
}

object GoogleService {
  private val gridCoordinate = new GridCoordinate()
    .setSheetId(0)
    .setRowIndex(0)
    .setColumnIndex(0)

  def batchUpdateRequest(rows: util.List[RowData]): BatchUpdateSpreadsheetRequest =
    new BatchUpdateSpreadsheetRequest().setRequests(requests(rows))

  private def requests(rows: util.List[RowData]): util.List[Request] =
    Seq(new Request().setUpdateCells(updateCellsRequest(rows))).asJava

  private def updateCellsRequest(rows: util.List[RowData]): UpdateCellsRequest =
    new UpdateCellsRequest()
      .setStart(gridCoordinate)
      .setRows(rows)
      .setFields("userEnteredValue")
}
