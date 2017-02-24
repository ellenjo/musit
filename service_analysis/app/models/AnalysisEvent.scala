package models

import no.uio.musit.models.{ActorId, EventId, MusitId}
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}

trait AnalysisEvent {
  val id: Option[EventId]
  val eventType: AnalysisEventType
  // val doneBy: Option[ActorId]
  val eventDate: DateTime
  // val affectedThing: Option[Seq[MusitId]]
  val registeredBy: Option[ActorId]
  val registeredDate: Option[DateTime]
  val note: Option[String]
  val externalReference: Option[String]
  //val attributes: Option[Seq[EventAttribute]]
}
