package models

import play.api.libs.json.{Format, Json}

case class Category(name: String)

object Category {

  implicit val jsFormat: Format[Category] = Json.format[Category]
}
