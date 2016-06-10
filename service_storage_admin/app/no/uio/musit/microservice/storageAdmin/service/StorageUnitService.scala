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
package no.uio.musit.microservice.storageAdmin.service

import no.uio.musit.microservice.storageAdmin.dao.StorageUnitDao
import no.uio.musit.microservice.storageAdmin.domain.{ Building, Room, _ }
import no.uio.musit.microservices.common.domain.MusitError
import no.uio.musit.microservices.common.extensions.FutureExtensions._
import no.uio.musit.microservices.common.utils.Misc._
import no.uio.musit.microservices.common.utils.{ ErrorHelper, ServiceHelper }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait StorageUnitService {
  //A separate function for this message because we want to verify we get this error message in some of the integration tests
  def unknownStorageUnitMsg(id: Long) = s"Unknown storageUnit with id: $id"

  private def storageUnitNotFoundError(id: Long): MusitError = {
    ErrorHelper.notFound(unknownStorageUnitMsg(id))
  }

  private def storageRoomNotFoundError(id: Long): MusitError = {
    ErrorHelper.notFound(s"Unknown storageRoom with id: $id")
  }

  private def storageBuildingNotFoundError(id: Long): MusitError = {
    ErrorHelper.notFound(s"Unknown storageBuilding with id: $id")
  }

  private def storageUnitTypeMismatch(id: Long, expected: StorageUnitType, inDatabase: StorageUnitType): MusitError = {
    ErrorHelper.conflict(s"StorageUnit with id: $id was expected to have storage type: ${expected.typename}, but had the type: ${inDatabase.typename} in the database.")
  }

  def create(storageUnit: StorageUnit): Future[Either[MusitError, StorageUnitTriple]] = {
    ServiceHelper.daoInsert(StorageUnitDao.insert(storageUnit)).futureEitherMap(StorageUnitTriple.createStorageUnit)
  }

  def createStorageTriple(storageTriple: StorageUnitTriple): Future[Either[MusitError, StorageUnitTriple]] = {
    val storageUnit = storageTriple.storageUnit
    storageTriple.storageKind match {
      case StUnit => create(storageUnit)
      case Room => RoomService.create(storageUnit, storageTriple.getRoom)
      case Building => BuildingService.create(storageUnit, storageTriple.getBuilding)
    }
  }

  def getChildren(id: Long): Future[Seq[StorageUnit]] = {
    StorageUnitDao.getChildren(id)
  }

  private def getStorageUnitOnly(id: Long) = StorageUnitDao.getStorageUnitOnlyById(id).toFutureEither(storageUnitNotFoundError(id))

  private def getBuildingById(id: Long) = StorageUnitDao.getBuildingById(id).toFutureEither(storageBuildingNotFoundError(id))

  private def getRoomById(id: Long) = StorageUnitDao.getRoomById(id).toFutureEither(storageRoomNotFoundError(id))

  def getById(id: Long): Future[Either[MusitError, StorageUnitTriple]] = {
    val futureEitherStorageUnit = getStorageUnitOnly(id)

    futureEitherStorageUnit.futureEitherFlatMap { storageUnit =>
      storageUnit.storageKind match {
        case StUnit => Future.successful(Right(StorageUnitTriple.createStorageUnit(storageUnit)))
        case Building => getBuildingById(id).futureEitherMap(storageBuilding => StorageUnitTriple.createBuilding(storageUnit, storageBuilding))
        case Room => getRoomById(id).futureEitherMap(storageRoom => StorageUnitTriple.createRoom(storageUnit, storageRoom))
      }
    }
  }

  def getStorageType(id: Long): Future[Either[MusitError, StorageUnitType]] = StorageUnitDao.getStorageType(id).toFutureEither(storageUnitNotFoundError(id))

  def all: Future[Seq[StorageUnit]] = {
    StorageUnitDao.all()
  }

  def find(id: Long) = {
    getById(id)
  }

  def updateStorageUnitByID(id: Long, storageUnit: StorageUnit) = {
    ServiceHelper.daoUpdate(StorageUnitDao.updateStorageUnit, id, storageUnit)
  }

  /*Verifies that the storage unit with the given id has the storage type expectedStorageUnitType.
   Else a Future false "MusitBoolean" is returned. */
  def verifyStorageTypeMatchesDatabase(id: Long, expectedStorageUnitType: StorageUnitType): Future[Either[MusitError, Boolean]] = {
    getStorageType(id).futureEitherFlatMapEither {
      storageUnitTypeInDatabase => boolToMusitBool(expectedStorageUnitType == storageUnitTypeInDatabase, storageUnitTypeMismatch(id, expectedStorageUnitType, storageUnitTypeInDatabase))
    }
  }

  def updateStorageTripleByID(id: Long, triple: StorageUnitTriple) = {
    verifyStorageTypeMatchesDatabase(id, triple.storageKind).futureEitherFlatMap { _ =>

      val modifiedTriple = triple.copyWithId(id) //We want the id in the url to override potential mistake in the body (of the original http request).

      val storageUnit = modifiedTriple.storageUnit

      modifiedTriple.storageKind match {
        case StUnit => updateStorageUnitByID(id, storageUnit)
        case Building => BuildingService.updateBuildingByID(id, (storageUnit, modifiedTriple.getBuilding))
        case Room => RoomService.updateRoomByID(id, (storageUnit, modifiedTriple.getRoom))
      }
    }
  }

  def deleteStorageTriple(id: Long): Future[Either[MusitError, Int]] = {
    StorageUnitDao.deleteStorageUnit(id).map(Right(_))
    /*At least for the moment, StorageUnitDao.deleteStorageUnit doesn't signal any other kind of errors other than what can be
     encoded in the Future[Int], so we unconditionally treat it as a "successfull" (Right) Int. Callers need to interpret the status Int.
     (It embeds a Future[Int] into a MusitFuture[Int] in the trivial way)
      */
  }
}

object StorageUnitService extends StorageUnitService {
}

trait RoomService {
  def create(storageUnit: StorageUnit, storageRoom: StorageRoom): Future[Either[MusitError, StorageUnitTriple]] = {
    ServiceHelper.daoInsert(StorageUnitDao.insertRoom(storageUnit, storageRoom))
  }

  def updateRoomByID(id: Long, storageUnitAndRoom: (StorageUnit, StorageRoom)) = {
    ServiceHelper.daoUpdate(StorageUnitDao.updateRoom, id, storageUnitAndRoom)
  }
}

object RoomService extends RoomService

trait BuildingService {
  def create(storageUnit: StorageUnit, storageBuilding: StorageBuilding): Future[Either[MusitError, StorageUnitTriple]] = {
    ServiceHelper.daoInsert(StorageUnitDao.insertBuilding(storageUnit, storageBuilding))
  }

  def updateBuildingByID(id: Long, storageUnitAndBuilding: (StorageUnit, StorageBuilding)) = {
    ServiceHelper.daoUpdate(StorageUnitDao.updateBuilding, id, storageUnitAndBuilding)
  }
}

object BuildingService extends BuildingService
