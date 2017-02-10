package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class Taxon(
  id: String,
  taxonId: Int,
  // miscellaneous attributes...
  higherClassification: Option[Seq[Taxon]]
)

object Taxon {

  implicit val reads: Reads[Taxon] = (
    (__ \ "Id").read[String] and
    (__ \ "taxonID").read[Int] and
    (__ \ "higherClassification").lazyReadNullable(Reads.seq[Taxon](reads))
  )(Taxon.apply _)

  implicit val writes: Writes[Taxon] = (
    (__ \ "id").write[String] and
    (__ \ "taxonId").write[Int] and
    (__ \ "higherClassification").lazyWriteNullable(Writes.seq[Taxon](writes))
  )(unlift(Taxon.unapply))
}
