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

package services

import com.google.inject.Inject
import models.event.dto.DtoConverters
import models.event.move.{MoveEvent, MoveNode, MoveObject}
import models.storage._
import models.{FacilityLocation, LocationHistory, ObjectsLocation}
import no.uio.musit.MusitResults._
import no.uio.musit.functional.Implicits.futureMonad
import no.uio.musit.functional.MonadTransformers.MusitResultT
import no.uio.musit.models._
import no.uio.musit.security.AuthenticatedUser
import no.uio.musit.time.dateTimeNow
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import repositories.dao.caching.LocalObjectDao
import repositories.dao.event.EventDao
import repositories.dao.storage._

import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * TODO: Document me!!!
 */
class StorageNodeService @Inject() (
    val unitDao: StorageUnitDao,
    val roomDao: RoomDao,
    val buildingDao: BuildingDao,
    val orgDao: OrganisationDao,
    val envReqService: EnvironmentRequirementService,
    val eventDao: EventDao,
    val localObjectDao: LocalObjectDao
) extends NodeService {

  val logger = Logger(classOf[StorageNodeService])

  /**
   * Simple check to see if a node with the given exists in a museum.
   *
   * @param mid
   * @param id
   * @return
   */
  def exists(mid: MuseumId, id: StorageNodeId): Future[MusitResult[Boolean]] = {
    unitDao.exists(mid, id)
  }

  /**
   * Adds a Root node to a Museum.
   *
   * @param mid
   * @param root
   * @return
   */
  def addRoot(
    mid: MuseumId,
    root: RootNode
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[RootNode]]] = {
    val theRoot = root.setUpdated(
      by = Some(currUsr.id),
      date = Some(dateTimeNow)
    )

    unitDao.insertRoot(mid, theRoot).flatMap { nodeId =>
      logger.debug(s"Updated root path...looking up node with ID $nodeId")
      unitDao.findRootNode(nodeId)
  }

  /**
   * TODO: Document me!
   */
  def addStorageUnit(
    mid: MuseumId,
    storageUnit: StorageUnit
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[StorageUnit]]] = {
    addNode[StorageUnit](
      mid = mid,
      node = storageUnit.copy(
        updatedBy = Some(currUsr.id),
        updatedDate = Some(dateTimeNow),
        nodeId = storageUnit.nodeId.orElse(Some(StorageNodeId.generate()))
      ),
      insert = unitDao.insert,
      setEnvReq = (node, mer) => node.copy(environmentRequirement = mer),
      updateWithPath = (id, path) => unitDao.setPath(id, path),
      getNode = getStorageUnitById
    )
  }

  /**
   * TODO: Document me!!!
   */
  def addRoom(
    mid: MuseumId,
    room: Room
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[Room]]] = {
    val test = room.copy(
      updatedBy = Some(currUsr.id),
      updatedDate = Some(dateTimeNow),
      nodeId = room.nodeId.orElse(Some(StorageNodeId.generate()))
    )
    addNode[Room](
      mid = mid,
      node = room.copy(
        updatedBy = Some(currUsr.id),
        updatedDate = Some(dateTimeNow)
      ),
      insert = roomDao.insert,
      setEnvReq = (node, mer) => node.copy(environmentRequirement = mer),
      updateWithPath = (id, path) => roomDao.setPath(id, path),
      getNode = getRoomByUuid
    )
  }

  /**
   * TODO: Document me!!!
   */
  def addBuilding(
    mid: MuseumId,
    building: Building
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[Building]]] = {
    addNode[Building](
      mid = mid,
      node = building.copy(
        updatedBy = Some(currUsr.id),
        updatedDate = Some(dateTimeNow)
      ),
      insert = buildingDao.insert,
      setEnvReq = (node, maybeEnvReq) => node.copy(environmentRequirement = maybeEnvReq),
      updateWithPath = (id, path) => buildingDao.setPath(id, path),
      getNode = getBuildingByUuid
    )
  }

  /**
   * TODO: Document me!!!
   */
  def addOrganisation(
    mid: MuseumId,
    organisation: Organisation
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[Organisation]]] = {
    addNode[Organisation](
      mid = mid,
      node = organisation.copy(
        updatedBy = Some(currUsr.id),
        updatedDate = Some(dateTimeNow)
      ),
      insert = orgDao.insert,
      setEnvReq = (node, mer) => node.copy(environmentRequirement = mer),
      updateWithPath = (id, path) => orgDao.setPath(id, path),
      getNode = getOrganisationByUuid
    )
  }

  /**
   * TODO: Document me!
   */
  def updateStorageUnit(
    mid: MuseumId,
    id: StorageNodeId,
    storageUnit: StorageUnit
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[StorageUnit]]] = {
    val su = storageUnit.copy(
      updatedBy = Some(currUsr.id),
      updatedDate = Some(dateTimeNow)
    )
    unitDao.update(mid, id, su).flatMap {
      case MusitSuccess(maybeRes) =>
        maybeRes.map { numUpdated =>
          for {
            _ <- su.environmentRequirement.map(er => saveEnvReq(mid, id, er))
              .getOrElse(Future.successful(None))
            node <- getStorageUnitById(mid, id)
          } yield {
            node
          }
        }.getOrElse(Future.successful(MusitSuccess(None)))

      case err: MusitError =>
        Future.successful(err)
    }
  }

  /**
   * TODO: Document me!!!
   */
  def updateRoom(
    mid: MuseumId,
    id: StorageNodeId,
    room: Room
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[Room]]] = {
    val updateRoom = room.copy(
      updatedBy = Some(currUsr.id),
      updatedDate = Some(dateTimeNow)
    )
    roomDao.update(mid, id, updateRoom).flatMap {
      case MusitSuccess(maybeRes) =>
        maybeRes.map { numUpdated =>
          for {
            _ <- updateRoom.environmentRequirement.map(er => saveEnvReq(mid, id, er))
              .getOrElse(Future.successful(None))
            node <- getRoomByUuid(mid, id)
          } yield {
            node
          }
        }.getOrElse(Future.successful(MusitSuccess(None)))

      case err: MusitError =>
        Future.successful(err)
    }
  }

  /**
   * TODO: Document me!!!
   */
  def updateBuilding(
    mid: MuseumId,
    id: StorageNodeId,
    building: Building
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[Building]]] = {
    val updateBuilding = building.copy(
      updatedBy = Some(currUsr.id),
      updatedDate = Some(dateTimeNow)
    )
    buildingDao.update(mid, id, updateBuilding).flatMap {
      case MusitSuccess(maybeRes) =>
        maybeRes.map { numUpdated =>
          for {
            _ <- updateBuilding.environmentRequirement.map(er => saveEnvReq(mid, id, er))
              .getOrElse(Future.successful(None))
            node <- getBuildingById(mid, id)
          } yield {
            node
          }
        }.getOrElse(Future.successful(MusitSuccess(None)))

      case err: MusitError =>
        Future.successful(err)
    }
  }

  /**
   * TODO: Document me!!!
   */
  def updateOrganisation(
    mid: MuseumId,
    id: StorageNodeId,
    organisation: Organisation
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[Organisation]]] = {
    val updateOrg = organisation.copy(
      updatedBy = Some(currUsr.id),
      updatedDate = Some(dateTimeNow)
    )
    orgDao.update(mid, id, updateOrg).flatMap {
      case MusitSuccess(maybeRes) =>
        maybeRes.map { numUpdated =>
          for {
            _ <- updateOrg.environmentRequirement.map(er => saveEnvReq(mid, id, er))
              .getOrElse(Future.successful(None))
            node <- getOrganisationById(mid, id)
          } yield {
            node
          }
        }.getOrElse(Future.successful(MusitSuccess(None)))

      case err: MusitError =>
        Future.successful(err)
    }
  }

  /**
   * TODO: Document me!
   */
  def getStorageUnitById(
    mid: MuseumId,
    id: StorageNodeId
  ): Future[MusitResult[Option[StorageUnit]]] = {
    val eventuallyUnit = unitDao.getById(mid, id)
    nodeById(mid, id, eventuallyUnit) { (n, maybeReq, maybeNames) =>
      n.copy(
        environmentRequirement = maybeReq,
        pathNames = maybeNames
      )
    }
  }

  /**
   * TODO: Document me!!!
   */
  def getRoomById(
    mid: MuseumId,
    id: StorageNodeId
  ): Future[MusitResult[Option[Room]]] = {
    val eventuallyRoom = roomDao.getById(mid, id)
    nodeById(mid, id, eventuallyRoom) { (n, maybeReq, maybeNames) =>
      n.copy(
        environmentRequirement = maybeReq,
        pathNames = maybeNames
      )
    }
  }

  /**
   * TODO: Document me!!!
   */
  def getBuildingById(
    mid: MuseumId,
    id: StorageNodeId
  ): Future[MusitResult[Option[Building]]] = {
    val eventuallyBuilding = buildingDao.getById(mid, id)
    nodeById(mid, id, eventuallyBuilding) { (n, maybeReq, maybeNames) =>
      n.copy(
        environmentRequirement = maybeReq,
        pathNames = maybeNames
      )
    }
  }
  /**
   * TODO: Document me!!!
   */
  def getOrganisationById(
    mid: MuseumId,
    id: StorageNodeId
  ): Future[MusitResult[Option[Organisation]]] = {
    val eventuallyOrg = orgDao.getById(mid, id)
    nodeById(mid, id, eventuallyOrg) { (n, maybeReq, maybeNames) =>
      n.copy(
        environmentRequirement = maybeReq,
        pathNames = maybeNames
      )
    }
  }

  /**
   * TODO: Document me!
   */
  def getNodeById(
    mid: MuseumId,
    id: StorageNodeId
  ): Future[MusitResult[Option[StorageNode]]] = {
    unitDao.getStorageTypeFor(mid, id).map {
      case MusitSuccess(maybeType) =>
        logger.debug(s"Disambiguating StorageType $maybeType")

        maybeType.map(_._2).map {
          case StorageType.RootType =>
            unitDao.findRootNode(id)

          case StorageType.RootLoanType =>
            unitDao.findRootNode(id)

          case StorageType.OrganisationType =>
            getOrganisationById(mid, id)

          case StorageType.BuildingType =>
            getBuildingById(mid, id)

          case StorageType.RoomType =>
            getRoomById(mid, id)

          case StorageType.StorageUnitType =>
            getStorageUnitById(mid, id)

        }.getOrElse {
          logger.warn(s"Could not resolve StorageType $maybeType")
          Future.successful(MusitSuccess(None))
        }
      case err: MusitError =>
        logger.debug(s"Node $id not found")
        MusitSuccess(None)
    }
  }

  def getNodeByStorageNodeId(
    mid: MuseumId,
    uuid: StorageNodeId
  ): Future[MusitResult[Option[StorageNode]]] = {
    (for {
      tuple <- MusitResultT(unitDao.getStorageTypeFor(mid, uuid))
      node <- tuple.map(t => MusitResultT(getNodeById(mid, t._1))).getOrElse {
        MusitResultT(
          Future.successful[MusitResult[Option[StorageNode]]](MusitSuccess(None))
        )
      }
    } yield node).value
  }

  def getNodeByOldBarcode(
    mid: MuseumId,
    oldBarcode: Long
  ): Future[MusitResult[Option[StorageNode]]] = {
    (for {
      tuple <- MusitResultT(unitDao.getStorageTypeFor(mid, oldBarcode))
      node <- tuple.map(t => MusitResultT(getNodeById(mid, t._1))).getOrElse {
        MusitResultT(
          Future.successful[MusitResult[Option[StorageNode]]](MusitSuccess(None))
        )
      }
    } yield node).value
  }

  /**
   * TODO: Document me!
   */
  def rootNodes(mid: MuseumId): Future[Seq[RootNode]] = {
    unitDao.findRootNodes(mid).map(_.sortBy(_.storageType.displayOrder))
  }

  /**
   * TODO: Document me!
   */
  def getChildren(
    mid: MuseumId,
    id: StorageNodeDatabaseId,
    page: Int,
    limit: Int
  ): Future[PagedResult[GenericStorageNode]] = {
    unitDao.getChildren(mid, id, page, limit)
  }

  /**
   * TODO: Document me!
   *
   * returns Some(-1) if the node has children and cannot be removed.
   * returns Some(1) when the node was successfully marked as removed.
   * returns None if the node isn't found.
   */
  def deleteNode(
    mid: MuseumId,
    id: StorageNodeDatabaseId
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Option[Int]]] = {
    unitDao.getById(mid, id).flatMap {
      case Some(node) =>
        isEmpty(node).flatMap { empty =>
          if (empty) {
            unitDao.markAsDeleted(currUsr.id, mid, id).map(_.map(Some.apply))
          } else {
            Future.successful(MusitSuccess(Some(-1)))
          }
        }

      case None =>
        Future.successful(MusitSuccess(None))
    }
  }

  /**
   * Helper to encapsulate shared logic between the public move methods.
   */
  private def persistMoveEvent[ID <: MusitId](
    mid: MuseumId,
    id: ID,
    event: MoveEvent
  )(f: EventId => Future[MusitResult[EventId]]): Future[MusitResult[EventId]] = {
    val dto = DtoConverters.MoveConverters.moveToDto(event)
    eventDao.insertEvent(mid, dto).flatMap(eventId => f(eventId)).recover {
      case NonFatal(ex) =>
        val msg = s"An exception occurred trying to move $id"
        logger.error(msg, ex)
        MusitInternalError(msg)
    }
  }

  private def persistMoveEvents[ID <: MusitUUID, E <: MoveEvent](
    mid: MuseumId,
    events: Seq[E]
  )(
    f: Seq[EventId] => MusitResult[Seq[ID]]
  ): Future[MusitResult[Seq[ID]]] = {
    val dtos = events.map(DtoConverters.MoveConverters.moveToDto)
    eventDao.insertEvents(mid, dtos).map(ids => f(ids)).recover {
      case NonFatal(ex) =>
        val msg = "An exception occurred registering a batch move with ids: " +
          s" ${events.map(_.id.getOrElse("<empty>")).mkString(", ")}"
        logger.error(msg, ex)
        MusitInternalError(msg)
    }
  }

  def moveNodes(
    mid: MuseumId,
    destination: StorageNodeDatabaseId,
    moveEvents: Seq[MoveNode]
  )(
    implicit
    currUsr: AuthenticatedUser
  ): Future[MusitResult[Seq[StorageNodeId]]] = {
    // Calling get on affectedThing, after filtering out nonEmpty ones, is safe.
    val nodeIds = moveEvents.filter(_.affectedThing.nonEmpty).map(_.affectedThing.get) // scalastyle:ignore
    val affectedNodes = unitDao.getNodesByUuids(mid, nodeIds)
    val currLoc = affectedNodes.map(_.map(n => (n.nodeId.get, n.isPartOf)).toMap)

    logger.debug(s"Preparing to move nodes to $destination")

    moveBatch(mid, destination, nodeIds, currLoc, moveEvents) {
      case (to, curr, events) =>
        logger.debug(s"Destination node is ${to.id} with path ${to.path}")
        logger.debug(s"Filtering away invalid placement of nodes in $destination.")
        // Filter away nodes that didn't pass first round of validation
        val nodesToMove = affectedNodes.map { nodes =>
          nodes.filter(n => events.exists(_.affectedThing.contains(n.nodeId.get)))
        }

        // Filter away nodes with invalid positions and process the ones remaining
        filterInvalidPosition(mid, to.path, nodesToMove).flatMap { validNodes =>
          if (validNodes.nonEmpty) {
            logger.debug(s"Will move ${validNodes.size} to $destination.")
            // Remove events for which moving the node was identified as invalid.
            val validEvents = events.filter(e => validNodes.exists(_.nodeId == e.affectedThing))
            val moveIds = validNodes.flatMap(_.nodeId)

            for {
              // Update the NodePath and partOf for all nodes to be moved.
              resLocUpd <- unitDao.batchUpdateLocation(validNodes, to)
              if resLocUpd.isSuccess
              // If the above update succeeded, we store the move events.
              mvRes <- persistMoveEvents(mid, validEvents)(_ => MusitSuccess(moveIds))
            } yield {
              logger.debug(s"Successfully moved ${validNodes.size} nodes to $destination")
              mvRes
            }
          } else {
            Future.successful {
              MusitValidationError("No valid commands were found. No nodes were moved.")
            }
          }
        }
    }
  }

  /**
   * Moves a batch of objects from their current locations to another node in
   * the storage facility. Note that there are no integrity checks here. The
   * objects are assumed to exist, and will be placed in the node regardless if
   * the exist or not.
   *
   * @param mid MuseumId
   * @param destination StorageNodeDatabaseId to place the objects
   * @param moveEvents A collection of MoveObject events.
   * @param currUsr the currently authenticated user.
   * @return A MusitResult with a collection of ObjectIds that were moved.
   */
  def moveObjects(
    mid: MuseumId,
    destination: StorageNodeDatabaseId,
    moveEvents: Seq[MoveObject]
  )(implicit currUsr: AuthenticatedUser): Future[MusitResult[Seq[ObjectUUID]]] = {
    // Calling get on affectedThing, after filtering out nonEmpty ones, is safe.
    val objIds = moveEvents.filter(_.affectedThing.nonEmpty).map(_.affectedThing.get) // scalastyle:ignore
    val currentLoc = localObjectDao.currentLocations(objIds)

    moveBatch(mid, destination, objIds, currentLoc, moveEvents) {
      case (_, _, events) =>
        persistMoveEvents(mid, events) { eventIds =>
          // Again the get on affectedThing is safe since we're guaranteed its
          // presence at this point.
          MusitSuccess(events.map(_.affectedThing.get)) // scalastyle:ignore
        }
    }
  }

  /**
   * Returns the
   *
   * @param oid the object ID to fetch history for
   * @return
   */
  def objectLocationHistory(
    mid: MuseumId,
    oid: ObjectUUID,
    limit: Option[Int]
  ): Future[MusitResult[Seq[LocationHistory]]] = {
    val res = eventDao.getObjectLocationHistory(mid, oid, limit).flatMap { events =>
      Future.sequence {
        events.map { e =>
          val fromTuple = findPathAndNames(mid, e.from)
          val toTuple = findPathAndNames(mid, Option(e.to))

          for {
            from <- fromTuple
            to <- toTuple
          } yield {
            LocationHistory(
              // registered by and date is required on Event, so they must be there.
              registeredBy = e.registeredBy.get,
              registeredDate = e.registeredDate.get,
              doneBy = e.doneBy,
              doneDate = e.doneDate,
              from = FacilityLocation(
                path = from._1,
                pathNames = from._2
              ),
              to = FacilityLocation(
                path = to._1,
                pathNames = to._2
              )
            )
          }
        }
      }
    }
    res.map(MusitSuccess.apply).recover {
      case NonFatal(ex) =>
        val msg = s"Fetching of location history for object $oid failed"
        logger.error(msg, ex)
        MusitInternalError(msg)
    }
  }

  /**
   *
   * @param mid
   * @param oid
   * @return
   */
  def currentObjectLocation(
    mid: MuseumId,
    oid: ObjectUUID
  ): Future[MusitResult[Option[StorageNode]]] = {
    val currentNodeId = localObjectDao.currentLocation(oid)
    currentNodeId.flatMap { optCurrentNodeId =>
      optCurrentNodeId.map { id =>
        getNodeById(mid, id)
      }.getOrElse(Future.successful(MusitSuccess(None)))
    }
  }

  /**
   *
   * @param mid
   * @param oids
   * @return
   */
  def currentObjectLocations(
    mid: MuseumId,
    oids: Seq[ObjectUUID]
  ): Future[MusitResult[Seq[ObjectsLocation]]] = {
    localObjectDao.currentLocations(oids).flatMap { objNodeMap =>
      val nodeIds = objNodeMap.values.flatten.toSeq.distinct
      unitDao.getNodesByIds(mid, nodeIds).flatMap { nodes =>
        Future.sequence {
          nodes.map { node =>
            unitDao.namesForPath(node.path).map { namedPath =>
              val objects = objNodeMap.filter(_._2 == node.id).keys.toSeq
              // Copy node and set path to it
              ObjectsLocation(node.copy(pathNames = Option(namedPath)), objects)
            }
          }
        }.map(MusitSuccess.apply)
      }
    }
  }

  /**
   *
   * @param mid
   * @param searchStr
   * @param page
   * @param limit
   * @return
   */
  def searchByName(
    mid: MuseumId,
    searchStr: String,
    page: Int,
    limit: Int
  ): Future[MusitResult[Seq[GenericStorageNode]]] = {
    if (searchStr.length > 2) {
      unitDao.getStorageNodeByName(mid, searchStr, page, limit).map { sn =>
        MusitSuccess(sn)
      }
    } else {
      Future.successful {
        MusitValidationError(
          s"Too few letters in search string $searchStr storageNode name."
        )
      }
    }
  }

}
