import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import State.{Closed, Opened}
import ujson.Value.Value

case class Ticket(
    id: Double,
    state: State,
    title: String,
    description: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    labels: Seq[String],
    assignees: Seq[String],
    author: String,
    url: String,
    weight: Int
) {
  def getHyperLink: String = s"""=hyperlink("$url", "${id.toInt.toString}")"""
}

object Ticket {
  def isBacklogTicket(labels: Seq[String]): Boolean =
    !labels.exists(l => (l == "ToDo") | (l == "Doing") | (l == "Done") | (l == "emergency"))

  implicit class EnrichTicket(response: Value) {
    def toState: State = response.str match {
      case "opened" => Opened
      case "closed" => Closed
    }
    def toDescription: String    = response.strOpt.getOrElse("")
    def toWeight: Int            = if (response.isNull) 0 else response.num.toInt
    def toLabels: Seq[String]    = if (response.arr.nonEmpty) response.arr.map(_.str).toSeq else Seq.empty
    def toAssignees: Seq[String] = if (response.arr.nonEmpty) response.arr.map(_.obj("name").str).toSeq else Seq.empty
    def toLocalDateTime: LocalDateTime = {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      LocalDateTime.parse(response.str, formatter)
    }
  }
}
