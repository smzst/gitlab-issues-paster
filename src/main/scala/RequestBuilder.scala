import SheetsWrapper._
import com.google.api.services.sheets.v4.model.{CellData, RowData}

case class TableHeaderAndBody(value: Map[String, Seq[CellData]])

class RequestBuilder(
    val headerAndBody: TableHeaderAndBody = TableHeaderAndBody(
      Map.empty[String, Seq[CellData]].withDefaultValue(Seq.empty)
    )
) {
  def set(header: String, body: Seq[CellData]): RequestBuilder = {
    val updatedValue = headerAndBody.value + (header -> (headerAndBody.value(header) ++ body))
    new RequestBuilder(headerAndBody.copy(updatedValue))
  }

  def build(): Seq[RowData] = {
    val headerRow: Seq[RowData]     = generateRow(headerAndBody.value.keys.toSeq.map(generateStringCell))
    val bodyRows: Seq[Seq[RowData]] = headerAndBody.value.values.toSeq.map(SheetsWrapper.generateRow)

    headerRow ++ bodyRows.flatten
  }
}
