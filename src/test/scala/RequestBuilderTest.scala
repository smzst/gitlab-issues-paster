import com.google.api.services.sheets.v4.model.CellData
import org.scalatest.FlatSpec

class RequestBuilderTest extends FlatSpec {

  private def numberCells(n: Double*): Seq[CellData] = n.map(SheetsWrapper.generateNumberCell).toSeq

  "set" should "引数の 1 つ目に重複がない場合、それぞれの Map の配列を返す" in {
    val result = new RequestBuilder()
      .set("header1", numberCells(1))
      .set("header2", numberCells(2))
      .headerAndBody
      .value
    assert(result === Map("header1" -> numberCells(1), "header2" -> numberCells(2)))
  }
  it should "引数の 1 つ目に重複がある場合、それぞれの 2 つ目の引数を結合した Map の配列を返す" in {
    val result = new RequestBuilder()
      .set("header1", numberCells(1))
      .set("header1", numberCells(2))
      .set("header3", numberCells(3))
      .headerAndBody
      .value
    assert(result === Map("header1" -> numberCells(1, 2), "header3" -> numberCells(3)))
  }
}
