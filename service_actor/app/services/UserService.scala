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

import models.Person
import no.uio.musit.security.AuthenticatedUser

import scala.concurrent.Future

class UserService {

  /**
   * Gets an actor representing the current user. If a match isn't found in the
   * UserInfo database, it will create the entry based on the current users data.
   *
   * @param user the current AuthenticatedUser.
   * @return a Future Person representation of the current user.
   */
  def currenUserAsActor(user: AuthenticatedUser): Future[Person] = {
    // FIXME: We need to check BOTH the Actor table, and the UserInfo table for
    // users that match the userinfo data.
    // In case the current user doesn't exist anywhere, we must add it to the
    // USER_INFO table!!!!!!!
    Future.successful(Person.fromAuthUser(user))
  }
}
