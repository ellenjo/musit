package controllers

import com.google.inject.Inject
import models.Taxon
import no.uio.musit.MusitResults.{MusitError, MusitInternalError, MusitSuccess, MusitValidationError}
import no.uio.musit.security.Authenticator
import no.uio.musit.service.MusitController
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Result}
import services.TaxonService

import scala.util.control.NonFatal
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class TaxonController @Inject() (
    val authService: Authenticator,
    val taxonService: TaxonService
) extends MusitController {

  val logger = Logger(classOf[TaxonController])

  /**
   * Service for looking up taxons in the geo-location service provided by
   * artsdatabanken.no
   */
  def searchByTerm(search: Option[String]) = Action.async { implicit request =>
    val expression = search.getOrElse("")
    taxonService.searchByTerm(expression).map {
      case MusitSuccess(taxons) =>
        if (taxons.nonEmpty) Ok(Json.toJson(taxons))
        else NoContent

      case err: MusitError =>
        InternalServerError(Json.obj("message" -> err.message))
    }.recover {
      case NonFatal(ex) =>
        val msg = "An error occurred when searching for taxon"
        logger.error(msg, ex)
        InternalServerError(Json.obj("message" -> msg))
    }
  }

  def scientificNameSuggest(suggest: Option[String]) = Action.async { implicit request =>
    val expression = suggest.getOrElse("")
    taxonService.searchScientNameSuggest(expression).map {
      case MusitSuccess(sNameSuggestions) =>
        if (sNameSuggestions.nonEmpty) Ok(Json.toJson(sNameSuggestions))
        else NoContent

      case err: MusitError =>
        InternalServerError(Json.obj("message" -> err.message))
    }.recover {

      case NonFatal(ex) =>
        val msg = "An error occurred when searching for taxon"
        logger.error(msg, ex)
        InternalServerError(Json.obj("message" -> msg))
    }
  }

  def getScientificNameById(scientificNameId: Long) = Action.async { implicit request =>
    validateId(scientificNameId) { _ =>
      taxonService.getScientificNameById(scientificNameId).map {
        case MusitSuccess(maybeScientificName) =>
          maybeScientificName.map { scientificName =>
            Ok(Json.toJson(scientificName))
          }.getOrElse(NotFound)

        case MusitValidationError(msg, _, _) =>
          BadRequest(Json.obj("message" -> msg))

        case MusitInternalError(msg) =>
          InternalServerError(Json.parse(msg))

        case err: MusitError =>
          InternalServerError(Json.obj("message" -> err.message))
      }.recover {
        case NonFatal(ex) =>
          val msg = "An error occurred when searching for a spesific scientificNameId"
          logger.error(msg, ex)
          InternalServerError(Json.obj("message" -> msg))
      }
    }
  }

  def getTaxonById(id: Long) = Action.async { implicit request =>
    validateId(id) { _ =>
      taxonService.getTaxonById(id).map {
        case MusitSuccess(maybetaxon) =>
          maybetaxon.map { taxon =>
            Ok(Json.toJson(taxon))
          }.getOrElse(NotFound)

        case MusitValidationError(msg, _, _) =>
          BadRequest(Json.obj("message" -> msg))

        case MusitInternalError(msg) =>
          InternalServerError(Json.parse(msg))

        case err: MusitError =>
          InternalServerError(Json.obj("message" -> err.message))
      }.recover {
        case NonFatal(ex) =>
          val msg = "An error occurred when searching for a spesific taxon"
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
