package models

import no.uio.musit.models.{ActorId, EventId}
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}

case class DatingEvent(
  id: Option[EventId],
  eventType: AnalysisEventType,
  eventDate: DateTime,
  registeredBy: Option[ActorId],
  registeredDate: Option[DateTime],
  note: Option[String],
  externalReference: Option[String],
  age: Option[String]
) extends AnalysisEvent

object DatingEvent {

  implicit val jsFormat: Format[DatingEvent] = Json.format[DatingEvent]

}
