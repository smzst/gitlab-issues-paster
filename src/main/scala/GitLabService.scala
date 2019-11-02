import Ticket._
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
            "state"    -> "opened",
            "order_by" -> "relative_position",
            "per_page" -> "100",
            "page"     -> n.toString
          )
        )

      val values: ArrayBuffer[Value] = acc ++ ujson.read(r.text()).arr
      val totalPage: Int             = r.headers("x-total-pages")(0).toInt

      if (n == totalPage) values.toSeq else recursive(n + 1, values)
    }

    ujson.read(recursive(1, ArrayBuffer()))
  }

  def extractBackLogTickets(tickets: Value): ArrayBuffer[Ticket] =
    for {
      ticket      <- tickets.arr
      id          = ticket.obj("iid").num
      title       = ticket.obj("title").str
      description = ticket.obj("description").toDescription
      createdAt   = ticket.obj("created_at").toLocalDateTime
      updatedAt   = ticket.obj("updated_at").toLocalDateTime
      labels      = ticket.obj("labels").toLabels
      assignees   = ticket.obj("assignees").toAssignees
      author      = ticket.obj("author").obj("name").str
      url         = ticket.obj("web_url").str
      weight      = ticket.obj("weight").toWeight
      if isBacklogTicket(labels)
    } yield Ticket(id, title, description, createdAt, updatedAt, labels, assignees, author, url, weight)

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
}
