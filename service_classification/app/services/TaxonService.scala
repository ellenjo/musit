package services

import com.google.inject.Inject
import models.{ScientificName, Taxon}
import play.api.{Configuration, Logger}
import play.api.http.HeaderNames
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import no.uio.musit.MusitResults.{MusitInternalError, MusitResult, MusitSuccess, MusitValidationError}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.http.Status._
import TaxonService.SearchUrlArtsBank
import TaxonService.SearchUrlArtsbankSuggest
import TaxonService.SearchUrlArtsbankScientificName

class TaxonService @Inject() (config: Configuration, ws: WSClient) {

  val logger = Logger(classOf[TaxonService])

  def searchByTerm(expr: String): Future[MusitResult[Seq[Taxon]]] = {
    ws.url(SearchUrlArtsBank)
      .withHeaders(
        HeaderNames.ACCEPT -> "application/json"
      )
      .withQueryString(
        "ScientificName" -> expr
      )
      .get()
      .map { res =>
        res.status match {
          case OK =>
            logger.debug(s"Got response from artsBank:\n${Json.prettyPrint(res.json)}")
            val taxons = res.json.as[Seq[Taxon]]
            MusitSuccess(taxons)

          case BAD_REQUEST =>
            MusitValidationError("bad request")

          case _ =>
            MusitInternalError("Something really bad")
        }
      }
  }

  def getScientificNameById(id: Long): Future[MusitResult[Option[ScientificName]]] = {
    ws.url(s"$SearchUrlArtsbankScientificName/${id.toString}")
      .withHeaders(
        HeaderNames.ACCEPT -> "application/json"
      )
      .get()
      .map { res =>
        res.status match {
          case OK =>
            logger.debug(s"Got response from artsBank:\n${Json.prettyPrint(res.json)}")
            res.json.validate[ScientificName] match {
              case JsSuccess(scientName, _) =>
                MusitSuccess(Option(scientName))

              case err: JsError =>
                if (res.body == "null") MusitSuccess(None)
                else MusitValidationError("bad response from artsdatabanken")
            }

          case BAD_REQUEST =>
            MusitValidationError("bad request")

          case _ =>
            MusitInternalError("Something really bad")
        }
      }
  }

  def searchScientNameSuggest(expr: String): Future[MusitResult[Seq[String]]] = {
    println(SearchUrlArtsbankSuggest)
    ws.url(SearchUrlArtsbankSuggest)
      .withHeaders(
        HeaderNames.ACCEPT -> "application/json"
      )
      .withQueryString(
        "ScientificName" -> expr
      )
      .get()
      .map { res =>
        res.status match {
          case OK =>
            logger.debug(s"Got response from artsBank:\n${Json.prettyPrint(res.json)}")
            val scientificnamesSuggestions = res.json.as[Seq[String]]
            MusitSuccess(scientificnamesSuggestions)

          case BAD_REQUEST =>
            MusitValidationError("bad request")

          case _ =>
            MusitInternalError("Something really bad")
        }
      }
  }

  def getTaxonById(id: Long): Future[MusitResult[Taxon]] = {
    ws.url(SearchUrlArtsBank + "/" + id.toString)
      .withHeaders(
        HeaderNames.ACCEPT -> "application/json"
      )
      .get()
      .map { res =>
        res.status match {
          case OK =>
            logger.debug(s"Got response from artsBank:\n${Json.prettyPrint(res.json)}")
            val taxons = res.json.as[Taxon]
            MusitSuccess(taxons)

          case BAD_REQUEST =>
            MusitValidationError("bad request")

          case _ =>
            MusitInternalError("Something really bad")
        }
      }
  }

}

object TaxonService {
  val SearchUrlArtsBank = "http://artsdatabanken.no/Api/Taxon"
  val SearchUrlArtsbankScientificName = s"$SearchUrlArtsBank/ScientificName"
  val SearchUrlArtsbankSuggest = s"$SearchUrlArtsbankScientificName/Suggest"

}