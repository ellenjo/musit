package services

import com.google.inject.Inject
import models.GeneralEvent
import no.uio.musit.MusitResults.MusitResult
import no.uio.musit.models.EventId
import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient

import scala.concurrent.Future

class EventService  @Inject() (config: Configuration, ws: WSClient) {

  val logger = Logger(classOf[EventService])

  def getAnalysisById(eventId: EventId) : Future[MusitResult[Option[GeneralEvent]]] = {
    ???
}
