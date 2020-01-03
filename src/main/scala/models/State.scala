package models

sealed abstract class State(val value: String)

object State {
  case object Opened extends State("opened")
  case object Closed extends State("closed")
}