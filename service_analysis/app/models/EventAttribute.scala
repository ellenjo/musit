package models

import play.api.libs.json.{Format, Json}

case class EventAttribute(key: String, value: String)

object EventAttribute {
  implicit val format: Format[EventAttribute] = Json.format[EventAttribute]
}
