package models

import play.api.libs.json.Json

case class Taxon(
Key: String,
value: String
)

object Taxon {
  implicit val format = Json.format[Taxon]
}
