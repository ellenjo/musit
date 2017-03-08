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

package repositories.dao.storage

import com.google.inject.{Inject, Singleton}
import models.storage.Organisation
import models.storage.dto.{ExtendedStorageNode, OrganisationDto, StorageNodeDto}
import no.uio.musit.models.{MuseumId, NodePath, StorageNodeDatabaseId, StorageNodeId}
import no.uio.musit.MusitResults.{MusitDbError, MusitResult, MusitSuccess}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repositories.dao.StorageTables

import scala.concurrent.Future

/**
 * TODO: Document me!!!
 */
@Singleton
class OrganisationDao @Inject() (
    val dbConfigProvider: DatabaseConfigProvider
) extends StorageTables {

  import driver.api._

  val logger = Logger(classOf[OrganisationDao])

  private def updateAction(id: StorageNodeDatabaseId, org: OrganisationDto): DBIO[Int] = {
    organisationTable.filter { ot =>
      ot.id === id
    }.update(org)
  }

  private def insertAction(organisationDto: OrganisationDto): DBIO[Int] = {
    organisationTable += organisationDto
  }

  /**
   * TODO: Document me!!!
   */
  def getById(mid: MuseumId, id: StorageNodeId): Future[Option[Organisation]] = {
    val action = for {
      maybeUnitDto <- getUnitByUuidAction(mid, id)
      maybeOrgDto <- maybeUnitDto.map { u =>
        organisationTable.filter(_.id === u.id).result.headOption
      }.getOrElse(DBIO.successful(None))
    } yield {
      // Map the results into an ExtendedStorageNode type
      maybeUnitDto.flatMap(u =>
        maybeOrgDto.map(o => ExtendedStorageNode(u, o)))
    }
    // Execute the query
    db.run(action).map(_.map { unitOrgTuple =>
      StorageNodeDto.toOrganisation(unitOrgTuple)
    })
  }

  /**
   * TODO: Document me!!!
   */
  def update(
    mid: MuseumId,
    id: StorageNodeId,
    organisation: Organisation
  ): Future[MusitResult[Option[Int]]] = {
    val dbid = organisation.id.getOrElse(StorageNodeDatabaseId(0)) //Todo hack
    dbid match {
      case res: StorageNodeDatabaseId if res == 0 => Future(MusitDbError("No BuildingId"))
      case _ => {
        val extendedOrgDto = StorageNodeDto.fromOrganisation(mid, organisation, Some(dbid))
        val action = for {
          unitsUpdated <- updateNodeAction(mid, dbid, extendedOrgDto.storageUnitDto)
          orgsUpdated <- if (unitsUpdated > 0) updateAction(dbid, extendedOrgDto.extension) else DBIO.successful[Int](0) // scalastyle:ignore
        } yield orgsUpdated

        db.run(action.transactionally).map {
          case res: Int if res == 1 => MusitSuccess(Some(res))
          case res: Int if res == 0 => MusitSuccess(None)
          case res: Int =>
            val msg = wrongNumUpdatedRows(id, res)
            logger.warn(msg)
            MusitDbError(msg)
        }
      }
    }
  }

  /**
   * Updates the path for the given StorageNodeDatabaseId
   *
   * @param id   the StorageNodeDatabaseId to update
   * @param path the NodePath to set
   * @return MusitResult[Unit]
   */
  def setPath(id: StorageNodeDatabaseId, path: NodePath): Future[MusitResult[Unit]] = {
    db.run(updatePathAction(id, path)).map {
      case res: Int if res == 1 => MusitSuccess(())

      case res: Int =>
        val msg = wrongNumUpdatedRows(id, res)
        logger.warn(msg)
        MusitDbError(msg)
    }
  }

  /**
   * TODO: Document me!!!
   */
  def insert(mid: MuseumId, organisation: Organisation): Future[StorageNodeDatabaseId] = {
    val extendedDto = StorageNodeDto.fromOrganisation(mid, organisation)
    val query = for {
      nodeId <- insertNodeAction(extendedDto.storageUnitDto)
      extWithId <- DBIO.successful(extendedDto.extension.copy(id = Some(nodeId)))
      n <- insertAction(extWithId)
    } yield {
      nodeId
    }

    db.run(query.transactionally)
  }

}

