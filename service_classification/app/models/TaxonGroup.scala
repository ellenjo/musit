package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class TaxonGroup(
  id: String,
  taxonId: Int,
  // miscellaneous attributes...
  scientificNames: Option[Seq[Taxon]]
)

object TaxonGroup {
  implicit val reads: Reads[TaxonGroup] = (
    (__ \ "Id").read[String] and
    (__ \ "taxonID").read[Int] and
    (__ \ "scientificNames").readNullable[Seq[Taxon]]
  )(TaxonGroup.apply _)

  implicit val writes: Writes[TaxonGroup] = (
    (__ \ "id").write[String] and
    (__ \ "taxonId").write[Int] and
    (__ \ "scientificNames").writeNullable[Seq[Taxon]]
  )(unlift(TaxonGroup.unapply))
}
