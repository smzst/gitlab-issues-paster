import ujson.Value.Value

case class Ticket(id: Double, title: String, description: String, url: String, weight: Int, labels: Seq[String]) {
  def getHyperLink: String = s"""=hyperlink("$url", "${id.toInt.toString}")"""
}

object Ticket {
  def isBacklogTicket(labels: Seq[String]): Boolean =
    !labels.exists(l => (l == "ToDo") | (l == "Doing") | (l == "Done") | (l == "emergency"))

  implicit class EnrichTicket(response: Value) {
    def toWeight: Int         = if (response.isNull) 0 else response.num.toInt
    def toLabels: Seq[String] = response.arr.map(_.str).toSeq
  }
}
