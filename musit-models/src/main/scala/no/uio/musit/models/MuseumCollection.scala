/*
 * MUSIT is a museum database to archive natural and cultural history data.
 * Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package no.uio.musit.models

import no.uio.musit.models.MuseumCollections.Collection
import play.api.libs.json.{Format, Json}

case class MuseumCollection(
    uuid: CollectionUUID,
    name: Option[String],
    oldSchemaNames: Seq[Collection]
) {

  def schemaIds: Seq[Int] = oldSchemaNames.map(_.id).distinct

}

object MuseumCollection {

  implicit val format: Format[MuseumCollection] = Json.format[MuseumCollection]

  def fromTuple(t: (CollectionUUID, Option[String], Seq[Collection])): MuseumCollection = {
    MuseumCollection(
      uuid = t._1,
      name = t._2,
      oldSchemaNames = t._3
    )
  }

}
