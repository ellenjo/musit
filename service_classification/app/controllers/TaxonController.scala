package controllers

import com.google.inject.Inject
import models.Taxon
import no.uio.musit.MusitResults.{MusitError, MusitSuccess, MusitValidationError}
import no.uio.musit.security.Authenticator
import no.uio.musit.service.MusitController
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import services.TaxonService

import scala.util.control.NonFatal
import play.api.libs.concurrent.Execution.Implicits.defaultContext

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
      case MusitSuccess(taxons) => Ok(Json.toJson(taxons))
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
      case MusitSuccess(sNameSuggestions) => Ok(Json.toJson(sNameSuggestions))
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
    taxonService.getScientificNameById(scientificNameId).map {
      case MusitSuccess(maybeScientificName) =>
        maybeScientificName.map { scientificName =>
          Ok(Json.toJson(scientificName))
        }.getOrElse(NotFound)

      case err: MusitError =>
        InternalServerError(Json.obj("message" -> err.message))
    }.recover {
      case NonFatal(ex) =>
        val msg = "An error occurred when searching for a spesific scientificNameId"
        logger.error(msg, ex)
        InternalServerError(Json.obj("message" -> msg))
    }
  }

  def getTaxonById(id: Long) = Action.async { implicit request =>
    taxonService.getTaxonById(id).map {
      case MusitSuccess(maybetaxon) =>
        maybetaxon.map { taxon =>
          Ok(Json.toJson(taxon))
        }.getOrElse(NotFound)
      case valerr: MusitValidationError =>
        BadRequest(Json.obj("message" -> valerr.message))
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
