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

package repositories.dao

import java.util.UUID

import no.uio.musit.models._
import play.api.db.slick.HasDatabaseConfig
import slick.jdbc.JdbcProfile

/**
 * Working with some of the DAOs require implicit mappers to/from strongly
 * typed value types/classes.
 */
trait ColumnTypeMappers { self: HasDatabaseConfig[JdbcProfile] =>

  import profile.api._

  implicit lazy val storageNodeIdMapper: BaseColumnType[StorageNodeDatabaseId] =
    MappedColumnType.base[StorageNodeDatabaseId, Long](
      snid => snid.underlying,
      longId => StorageNodeDatabaseId(longId)
    )

  implicit lazy val objectIdMapper: BaseColumnType[ObjectId] =
    MappedColumnType.base[ObjectId, Long](
      oid => oid.underlying,
      longId => ObjectId(longId)
    )

  implicit lazy val eventIdMapper: BaseColumnType[EventId] =
    MappedColumnType.base[EventId, Long](
      eid => eid.underlying,
      longId => EventId(longId)
    )

  implicit val actorIdMapper: BaseColumnType[ActorId] =
    MappedColumnType.base[ActorId, String](
      aid => aid.asString,
      strId => ActorId(UUID.fromString(strId))
    )

  implicit lazy val museumIdMapper: BaseColumnType[MuseumId] =
    MappedColumnType.base[MuseumId, Int](
      museumId => museumId.underlying,
      id => MuseumId(id)
    )

  implicit lazy val nodePathMapper: BaseColumnType[NodePath] =
    MappedColumnType.base[NodePath, String](
      nodePath => nodePath.path,
      pathStr => NodePath(pathStr)
    )

  implicit lazy val museumNoMapper: BaseColumnType[MuseumNo] =
    MappedColumnType.base[MuseumNo, String](
      museumNo => museumNo.value,
      noStr => MuseumNo(noStr)
    )

  implicit lazy val subNoMapper: BaseColumnType[SubNo] =
    MappedColumnType.base[SubNo, String](
      subNo => subNo.value,
      noStr => SubNo(noStr)
    )
}
