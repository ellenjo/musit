package services

import com.google.inject.Inject
import models.TaxonGroup
import play.api.{Configuration, Logger}
import play.api.http.HeaderNames
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import TaxonService.SearchUrlArtsBank

class TaxonService @Inject() (config: Configuration, ws: WSClient) {

  val logger = Logger(classOf[TaxonService])

  def searchByTerm(expr: String): Future[Seq[TaxonGroup]] = {
    ws.url(SearchUrlArtsBank)
      .withHeaders(
        HeaderNames.ACCEPT -> "application/json"
      )
      .withQueryString(
        "term" -> expr
      )
      .get()
      .map { response =>
        val res = response.json.as[Seq[TaxonGroup]]
        // println(s"Got response from artsBank:\n${response.body}")
        //        logger.debug(s"Got response from artsBank:\n${response.body}")
        logger.debug(res.mkString("\n"))
        res
        /*(response.json \ "totaltAntallTreff").asOpt[String].map(_.toInt) match {
          case Some(numRes) if numRes > 0 =>
            logger.debug(s"Got $numRes taxon results.")
            val jsArr = (response.json \ "KeyValueOfstringstring").as[JsArray].value
            jsArr.foldLeft(List.empty[Taxon]) { (state, ajs) =>
              Json.fromJson[GeoNorwayAddress](ajs).asOpt.map { gna =>
                state :+ GeoNorwayAddress.asAddress(gna)
              }.getOrElse(state)
            }

          case _ =>
            logger.debug("Search did not return any results")
            Seq.empty
        }*/
      }
  }

}

object TaxonService {
  val SearchUrlArtsBank = "http://artsdatabanken.no/Api/Taxon"
}