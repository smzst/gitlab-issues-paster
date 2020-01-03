import java.time.LocalDateTime

case class Ticket(
    id: Int,
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
  val isBacklogTicket: Boolean =
    !labels.exists(l => (l == "ToDo") | (l == "Doing") | (l == "Done") | (l == "emergency"))
}
