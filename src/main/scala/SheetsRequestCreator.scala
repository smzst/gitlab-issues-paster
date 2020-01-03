import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import SheetsWrapper.{generateFormulaCell, generateNumberCell, generateRow, generateStringCell}
import com.google.api.services.sheets.v4.model.RowData
import models.{BacklogTickets, State, Ticket}
import ujson.Value.Value

class SheetsRequestCreator(tickets: Value) {

  private val headerContents =
    Seq("ID", "State", "Title", "Description", "Created at", "Updated at", "Labels", "Assignees", "Author", "Weight")

  def create(): Seq[RowData] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    val headerRows: Seq[RowData] = generateRow(headerContents.map(generateStringCell))
    val bodyRows: Seq[RowData] = extractBackLogTickets(tickets).value.flatMap(
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

    headerRows ++ bodyRows
  }

  private def extractBackLogTickets(response: Value): BacklogTickets = {
    val tickets = for {
      ticket      <- response.arr
      id          = ticket.obj("iid").num.toInt
      state       = toState(ticket.obj("state"))
      title       = ticket.obj("title").str
      description = toDescription(ticket.obj("description"))
      createdAt   = toLocalDateTime(ticket.obj("created_at"))
      updatedAt   = toLocalDateTime(ticket.obj("updated_at"))
      labels      = toLabels(ticket.obj("labels"))
      assignees   = toAssignees(ticket.obj("assignees"))
      author      = ticket.obj("author").obj("name").str
      url         = ticket.obj("web_url").str
      weight      = toWeight(ticket.obj("weight"))
    } yield Ticket(id, state, title, description, createdAt, updatedAt, labels, assignees, author, url, weight)

    BacklogTickets(tickets.toSeq)
  }

  private def toState(response: Value): State = response.str match {
    case "opened" => Opened
    case "closed" => Closed
  }
  private def toDescription(response: Value): String = response.strOpt.getOrElse("")
  private def toWeight(response: Value): Int         = if (response.isNull) 0 else response.num.toInt
  private def toLabels(response: Value): Seq[String] =
    if (response.arr.nonEmpty) response.arr.map(_.str).toSeq else Seq.empty
  private def toAssignees(response: Value): Seq[String] =
    if (response.arr.nonEmpty) response.arr.map(_.obj("name").str).toSeq else Seq.empty
  private def toLocalDateTime(response: Value): LocalDateTime = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime.parse(response.str, formatter)
  }

}
