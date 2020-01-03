import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import State.{Closed, Opened}
import ujson.Value.Value

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

class GitLabService(privateToken: String, domainName: String, apiVersion: String, projectId: String) {

  def fetch(): Value = {
    @tailrec
    def recursive(n: Int, acc: ArrayBuffer[Value]): Seq[Value] = {
      val r = requests
        .get(
          s"https://$domainName/api/$apiVersion/projects/$projectId/issues",
          headers = Map("PRIVATE-TOKEN" -> privateToken),
          params = Map(
            "order_by" -> "relative_position",
            "per_page" -> "100",
            "page"     -> n.toString
          )
        )

      val values: ArrayBuffer[Value] = acc ++ ujson.read(r.text()).arr
      val totalPage: Int             = r.headers("x-total-pages")(0).toInt

      Thread.sleep(500)
      if (n == totalPage) values.toSeq else recursive(n + 1, values)
    }

    ujson.read(recursive(1, ArrayBuffer()))
  }

  def extractBackLogTickets(response: Value): BacklogTickets = {
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

  def toJson(tickets: ArrayBuffer[Ticket]): Value = {
    ujson.Obj(
      "issues" -> tickets.map(
        t =>
          ujson.Obj(
            "title"       -> t.title,
            "description" -> t.description,
            "url"         -> t.url,
            "weight"      -> t.weight,
            "labels"      -> t.labels
          )
      )
    )
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
