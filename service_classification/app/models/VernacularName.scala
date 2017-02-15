package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class VernacularName(
  vernacularNameId: Int,
  taxonId: Int,
  vernacularName: String,
  nomenclaturalStatus: Option[String],
  language: Option[String],
  nameAccordingTo: Option[String]
)

object VernacularName {

  implicit val reads: Reads[VernacularName] = (
    (__ \ "vernacularNameID").read[Int] and
    (__ \ "taxonID").read[Int] and
    (__ \ "vernacularName").read[String] and
    (__ \ "nomenclaturalStatus").readNullable[String] and
    (__ \ "language").readNullable[String] and
    (__ \ "nameAccordingTo").readNullable[String]
  )(VernacularName.apply _)

  implicit val writes: Writes[VernacularName] = (
    (__ \ "vernacularNameId").write[Int] and
    (__ \ "taxonId").write[Int] and
    (__ \ "vernacularName").write[String] and
    (__ \ "nomenclaturalStatus").writeNullable[String] and
    (__ \ "language").writeNullable[String] and
    (__ \ "nameAccordingTo").writeNullable[String]
  )(unlift(VernacularName.unapply))
}

