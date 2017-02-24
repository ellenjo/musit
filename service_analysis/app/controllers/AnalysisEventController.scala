package controllers

import com.google.inject.Inject
import no.uio.musit.MusitResults.{MusitError, MusitInternalError, MusitSuccess, MusitValidationError}
import no.uio.musit.security.Authenticator
import no.uio.musit.service.MusitController
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Result}
import services.EventService

import scala.concurrent.Future
import scala.util.control.NonFatal

class AnalysisEventController @Inject() (
  val authService: Authenticator,
  val eventService: EventService
) extends MusitController {

  val logger = Logger(classOf[AnalysisEventController])

  def getById(mid: Int, eventId: Long)= Action.async { implicit request =>
    validateId(eventId) { _ =>
  eventService.getAnalysisById(eventId).map {
        case MusitSuccess(maybeAnalysis) =>
          maybeAnalysis.map { analysis =>
            Ok(Json.toJson(analysis))
          }.getOrElse(NotFound)

        case MusitValidationError(msg, _, _) =>
          BadRequest(Json.obj("message" -> msg))

        case MusitInternalError(msg) =>
          InternalServerError(Json.parse(msg))

        case err: MusitError =>
          InternalServerError(Json.obj("message" -> err.message))
      }.recover {
        case NonFatal(ex) =>
          val msg = "An error occurred when searching for a spesific analysis"
          logger.error(msg, ex)
          InternalServerError(Json.obj("message" -> msg))
      }
    }
  }

  def validateId(id: Long)(f: Long => Future[Result]): Future[Result] = {
    if (id > 0) f(id)
    else Future.successful {
      BadRequest(Json.obj("message" -> "Id must be a positive integer"))
    }
  }

}
