import com.google.api.services.sheets.v4.model.{CellData, ExtendedValue, RowData}

import scala.jdk.CollectionConverters._

object SheetsWrapper {
  // todo 多分いちいち new しなくてもいい箇所があると思うので検討
  def generateRow(cellData: Seq[CellData]): Seq[RowData] = Seq(new RowData().setValues(cellData.asJava))

  def generateStringCell(value: String): CellData =
    new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(value))

  def generateNumberCell(value: Double): CellData =
    new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue(value))

  def generateFormulaCell(value: String): CellData =
    new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue(value))

  def generateBoolCell(value: Boolean): CellData =
    new CellData().setUserEnteredValue(new ExtendedValue().setBoolValue(value))
}
