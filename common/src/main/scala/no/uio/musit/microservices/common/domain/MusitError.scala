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
package no.uio.musit.microservices.common.domain

import java.net.URI

import play.api.http.Status
import play.api.libs.json.{ Format, Json }

case class MusitError(status: Int = Status.BAD_REQUEST, message: String, private val developerMessage: String = "") {

  // TODO: check if in dev-environment!
  def inDevEnvironment = true

  def getDeveloperMessage = if (inDevEnvironment) developerMessage else ""
}

object MusitError {
  // TODO: Only include developerMessage if in devEnvironment!
  implicit val format: Format[MusitError] = Json.format[MusitError]
}

class MusitException(message: String, private val developerMessage: String = "") extends Throwable {
  def status = Status.BAD_REQUEST
  def toMusitError = MusitError(status, message, developerMessage)
}

class MusitNotFoundException(message: String, private val developerMessage: String = "") extends MusitException(message, developerMessage) {
  override def status = Status.NOT_FOUND
}

class MusitTooManyRecordsUpdatedException(message: String, private val developerMessage: String = "") extends MusitException(message, developerMessage) {
  override def status = Status.BAD_REQUEST
}

/**
 * May get thrown when our sevices try to do http requests to other servers (or another of our services), but they fail.
 * Used in say DataPorten calls, which may fail. Error represents the result from the "external" server
 */
// TODO: Replace the exceptions in PlayExtensions.scala with this one
class HttpCallException(val uri: Option[URI], error: MusitError) extends MusitException(error.message, "") {
  override def status = error.status //Propagates the status, that may be wrong in some circumstances?
  override def toMusitError = error
}
