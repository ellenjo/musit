package models

import no.uio.musit.models.{ActorId, EventId}
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}

case class GeneralEvent(
  id: Option[EventId],
  eventType: AnalysisEventType,
  eventDate: DateTime,
  registeredBy: Option[ActorId],
  registeredDate: Option[DateTime],
  note: Option[String],
  externalReference: Option[String],
  attributes: Option[Seq[EventAttribute]]
) extends AnalysisEvent

object GeneralEvent {

  implicit val jsFormat: Format[GeneralEvent] = Json.format[GeneralEvent]
}
