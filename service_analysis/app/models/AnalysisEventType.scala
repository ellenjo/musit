package models

import no.uio.musit.models._
import play.api.libs.json.{Format, Json}

case class AnalysisEventType(
  id: Int,
  shortName: Option[String],
  category: Category,
  attributes: Seq[EventAttribute]
)

object AnalysisEventType {

  implicit val jsFormat: Format[AnalysisEventType] = Json.format[AnalysisEventType]
}

