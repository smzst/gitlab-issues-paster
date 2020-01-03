package models

case class BacklogTickets(value: Seq[Ticket])

object BacklogTickets {
  def apply(value: Seq[Ticket]): BacklogTickets = new BacklogTickets(value.filter(_.isBacklogTicket))
}
