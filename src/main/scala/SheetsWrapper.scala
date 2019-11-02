import com.google.api.services.sheets.v4.model.{CellData, ExtendedValue, RowData}

import scala.jdk.CollectionConverters._

// todo 多分いちいち new しなくてもいい箇所があると思うので検討
object SheetsWrapper {

  /**
    * @param values String, Int, Double, Boolean, Seq[_] の配列である必要があります。
    * @return 配列同士の結合ができるように Seq[RowData] を返します。
    *         `asJava` を使って util.List[RowData] に変換してください。
    */
  def generateRow(values: Seq[Any]): Seq[RowData] =
    Seq(new RowData().setValues(values.map(generateCell).asJava))

  private def generateCell(value: Any): CellData = value match {
    case v: String if v.length == 0          => generateStringCell("")
    case v: String if v.head.toString == "=" => generateFormulaCell(v)
    case v: String                           => generateStringCell(v)
    case v: Double                           => generateNumberCell(v)
    case v: Int                              => generateNumberCell(v.toDouble)
    case v: Boolean                          => generateBoolCell(v)
    case v: Seq[_]                           => generateStringCell(v.mkString(", "))
    case _                                   => ???
  }

  private def generateStringCell(value: String): CellData =
    new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(value))

  private def generateNumberCell(value: Double): CellData =
    new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue(value))

  private def generateFormulaCell(value: String): CellData =
    new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue(value))

  private def generateBoolCell(value: Boolean): CellData =
    new CellData().setUserEnteredValue(new ExtendedValue().setBoolValue(value))
}
