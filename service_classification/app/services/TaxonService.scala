package services

import com.google.inject.Inject
import models.{ScientificName, Taxon}
import play.api.{Configuration, Logger}
import play.api.http.HeaderNames
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import no.uio.musit.MusitResults.{MusitInternalError, MusitResult, MusitSuccess, MusitValidationError}
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
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
        parseSeqResult[Taxon](res) { taxons =>
          MusitSuccess(taxons)
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
      .map(res => parseSeqResult[String](res)(names => MusitSuccess(names)))
  }

  def getScientificNameById(id: Long): Future[MusitResult[Option[ScientificName]]] = {
    ws.url(s"$SearchUrlArtsbankScientificName/${id.toString}")
      .withHeaders(
        HeaderNames.ACCEPT -> "application/json"
      )
      .get()
      .map { res =>
        parseOptionalResult[ScientificName](res) { scientName =>
          MusitSuccess(Option(scientName))
        }
      }
  }

  def getTaxonById(id: Long): Future[MusitResult[Option[Taxon]]] = {
    ws.url(SearchUrlArtsBank + "/" + id.toString)
      .withHeaders(
        HeaderNames.ACCEPT -> "application/json"
      )
      .get()
      .map(res => parseOptionalResult[Taxon](res)(t => MusitSuccess(Option(t))))
  }

  def parseResult[A](res: WSResponse)(ok: WSResponse => MusitResult[A]) = {
    res.status match {
      case OK => ok(res)
      case BAD_REQUEST => MusitValidationError("bad request")
      case _ =>
        logger.debug(s"unhandled ${res.status} ${res.body}")
        MusitInternalError(res.body)
    }
  }

  def parseSeqResult[A](res: WSResponse)(
    success: Seq[A] => MusitResult[Seq[A]]
  )(implicit reads: Reads[A]) = {
    parseResult[Seq[A]](res) { okRes =>
      logger.debug(s"Got response from artsBank:\n${Json.prettyPrint(res.json)}")
      val parsed = okRes.json.as[Seq[A]]
      MusitSuccess(parsed)
    }
  }

  def parseOptionalResult[A](res: WSResponse)(
    success: A => MusitResult[Option[A]]
  )(implicit reads: Reads[A]) = {
    parseResult[Option[A]](res) { okRes =>
      logger.debug(s"Got response from artsBank:\n${Json.prettyPrint(res.json)}")
      res.json.validate[A] match {
        case JsSuccess(valid, _) =>
          success(valid)

        case err: JsError =>
          if (res.body == "null") MusitSuccess(None)
          else MusitValidationError("bad response from artsdatabanken")
      }
    }
  }

}

object TaxonService {
  val SearchUrlArtsBank = "http://artsdatabanken.no/Api/Taxon"
  val SearchUrlArtsbankScientificName = s"$SearchUrlArtsBank/ScientificName"
  val SearchUrlArtsbankSuggest = s"$SearchUrlArtsbankScientificName/Suggest"

}