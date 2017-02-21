package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Taxon(
  id: String,
  taxonId: Int,
  scientificNames: Option[Seq[ScientificName]],
  vernacularNames: Option[Seq[VernacularName]],
  dynamicProperties: Option[Seq[TaxonProperty]],
  acceptedName: Option[ScientificName],
  preferredVernacularName: Option[VernacularName]
)

object Taxon {
  implicit val reads: Reads[Taxon] = (
    (__ \ "Id").readNullable[String].map(_.getOrElse("")) and
    (__ \ "taxonID").read[Int] and
    (__ \ "scientificNames").readNullable[Seq[ScientificName]] and
    (__ \ "vernacularNames").readNullable[Seq[VernacularName]] and
    (__ \ "dynamicProperties").readNullable[Seq[TaxonProperty]] and
    (__ \ "AcceptedName").readNullable[ScientificName] and
    (__ \ "PreferredVernacularName").readNullable[VernacularName]
  )(Taxon.apply _)

  implicit val writes: Writes[Taxon] = (
    (__ \ "id").write[String] and
    (__ \ "taxonId").write[Int] and
    (__ \ "scientificNames").writeNullable[Seq[ScientificName]] and
    (__ \ "vernacularNames").writeNullable[Seq[VernacularName]] and
    (__ \ "dynamicProperties").writeNullable[Seq[TaxonProperty]] and
    (__ \ "acceptedName").writeNullable[ScientificName] and
    (__ \ "preferredVernacularName").writeNullable[VernacularName]
  )(unlift(Taxon.unapply))
}
