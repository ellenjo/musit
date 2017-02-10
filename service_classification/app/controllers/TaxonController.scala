package controllers

import com.google.inject.Inject
import models.TaxonGroup
import no.uio.musit.security.Authenticator
import no.uio.musit.service.MusitController
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import services.TaxonService

import scala.util.control.NonFatal

class TaxonController  @Inject() (
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
    taxonService.searchByTerm(expression).map { taxons =>
      Ok(Json.toJson[Seq[TaxonGroup]](taxons))
    }.recover {
      case NonFatal(ex) =>
        val msg = "An error occurred when searching for taxon"
        logger.error(msg, ex)
        InternalServerError(Json.obj("message" -> msg))
    }
  }

}
