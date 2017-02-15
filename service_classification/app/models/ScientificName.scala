package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ScientificName(
  id: String,
  scientificNameId: Int,
  taxonId: Int,
  scientificName: String,
  scientificNameAuthorship: Option[String],
  taxonRank: Option[String],
  taxonomicStatus: Option[String],
  acceptedNameUsage: Option[ScientificName],
  higherClassification: Option[Seq[ScientificName]],
  nameAccordingTo: Option[String],
  dynamicProperties: Option[Seq[TaxonProperty]]
)
object ScientificName {

  implicit val reads: Reads[ScientificName] = (
    (__ \ "Id").readNullable[String].map(_.getOrElse("")) and
    (__ \ "scientificNameID").read[Int] and
    (__ \ "taxonID").read[Int] and
    (__ \ "scientificName").read[String] and
    (__ \ "scientificNameAuthorship").readNullable[String] and
    (__ \ "taxonRank").readNullable[String] and
    (__ \ "taxonomicStatus").readNullable[String] and
    (__ \ "acceptedNameUsage").lazyReadNullable(reads) and
    (__ \ "higherClassification").lazyReadNullable(Reads.seq[ScientificName](reads)) and
    (__ \ "nameAccordingTo").readNullable[String] and
    (__ \ "dynamicProperties").readNullable[Seq[TaxonProperty]]
  )(ScientificName.apply _)

  implicit val writes: Writes[ScientificName] = (
    (__ \ "id").write[String] and
    (__ \ "scientificNameId").write[Int] and
    (__ \ "taxonId").write[Int] and
    (__ \ "scientificName").write[String] and
    (__ \ "scientificNameAuthorship").writeNullable[String] and
    (__ \ "taxonRank").writeNullable[String] and
    (__ \ "taxonomicStatus").writeNullable[String] and
    (__ \ "acceptedNameUsage").lazyWriteNullable(writes) and
    (__ \ "higherClassification").lazyWriteNullable(Writes.seq[ScientificName](writes)) and
    (__ \ "nameAccordingTo").writeNullable[String] and
    (__ \ "dynamicProperties").writeNullable[Seq[TaxonProperty]]
  )(unlift(ScientificName.unapply))
}
