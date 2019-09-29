import java.util
import java.util.Collections

import com.google.api.services.sheets.v4.SheetsScopes
import com.typesafe.config.Config

case class GoogleServiceConfiguration(
    applicationName: String,
    credentialsFilePath: String,
    spreadSheetId: String,
    credentialStoreDirectory: String,
    scopes: util.List[String]
)

object GoogleServiceConfiguration {
  def apply(config: Config): GoogleServiceConfiguration = new GoogleServiceConfiguration(
    config.getString("application-name"),
    config.getString("credential-file-path"),
    config.getString("spread-sheet-id"),
    s"/tmp/.google_credentials/${config.getString("application-name")}",
    Collections.singletonList(SheetsScopes.SPREADSHEETS)
  )
}
