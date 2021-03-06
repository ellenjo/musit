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

package controllers.rest

import com.google.inject.Inject
import no.uio.musit.MusitResults.{MusitError, MusitSuccess}
import no.uio.musit.security.Authenticator
import no.uio.musit.security.Permissions.{GodMode, MusitAdmin}
import no.uio.musit.security.crypto.MusitCrypto
import no.uio.musit.service.MusitAdminController
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import repositories.dao.AuthDao

class CollectionController @Inject()(
    implicit val authService: Authenticator,
    val crypto: MusitCrypto,
    val dao: AuthDao
) extends MusitAdminController {

  val logger = Logger(classOf[CollectionController])

  private def serverError(msg: String): Result =
    InternalServerError(Json.obj("message" -> msg))

  /**
   * Fetch all MuseumCollections in the system
   */
  def getAllCollections = MusitSecureAction().async { implicit request =>
    dao.allCollections.map {
      case MusitSuccess(cols) => if (cols.nonEmpty) Ok(Json.toJson(cols)) else NoContent
      case err: MusitError    => serverError(err.message)
    }
  }

  def getCollection(
      colId: String
  ) = MusitSecureAction().async { implicit request =>
    ???
  }

  def addCollection =
    MusitAdminAction(MusitAdmin).async(parse.json) { implicit request =>
      ???
    }

  def updateCollection(
      colId: String
  ) = MusitAdminAction(MusitAdmin).async(parse.json) { implicit request =>
    ???
  }

  def removeCollection(
      colId: String
  ) = MusitAdminAction(GodMode).async { implicit request =>
    ???
  }

}
