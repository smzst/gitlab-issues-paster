package gitlab

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
}
