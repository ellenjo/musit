package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class TaxonProperty(
  name: String,
  value: String,
  properties: Option[Seq[TaxonProperty]]
)

object TaxonProperty {

  implicit val reads: Reads[TaxonProperty] = (
    (__ \ "Name").read[String] and
    (__ \ "Value").read[String] and
    (__ \ "Properties").lazyReadNullable(Reads.seq[TaxonProperty](reads))
  )(TaxonProperty.apply _)

  implicit val writes: Writes[TaxonProperty] = (
    (__ \ "name").write[String] and
    (__ \ "value").write[String] and
    (__ \ "properties").lazyWriteNullable(Writes.seq[TaxonProperty](writes))
  )(unlift(TaxonProperty.unapply))
}