import Ticket._
import ujson.Value.Value

import scala.collection.mutable.ArrayBuffer

class GitLabService(privateToken: String, domainName: String, apiVersion: String, groupId: String) {
  def fetch(): Value = ujson.read(
    requests
      .get(
        s"https://$domainName/api/$apiVersion/groups/$groupId/issues",
        headers = Map("PRIVATE-TOKEN" -> privateToken),
        params = Map("state"          -> "opened", "order_by" -> "relative_position", "per_page" -> "50")
      )
      .text()
  )

  def extractBackLogTickets(tickets: Value): ArrayBuffer[Ticket] =
    for {
      ticket      <- tickets.arr
      id          = ticket.obj("iid").num
      title       = ticket.obj("title").str
      description = ticket.obj("description").str
      url         = ticket.obj("web_url").str
      weight      = ticket.obj("weight").toWeight
      labels      = ticket.obj("labels").toLabels
      if isBacklogTicket(labels)
    } yield Ticket(id, title, description, url, weight, labels)

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
