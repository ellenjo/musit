/*
 *   MUSIT is a cooperation between the university museums of Norway.
 *   Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License,
 *   or any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License along
 *   with this program; if not, write to the Free Software Foundation, Inc.,
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package no.uio.musit.security

protected class SecurityContext(val userGroups: Seq[String]) {

  def hasAllGroups(groups: Seq[String]) = (groups==null) || groups.forall( g => userGroups.contains(g) )
  def hasNoneOfGroups(groups: Seq[String]) = (groups==null) || groups.forall( g => !userGroups.contains(g) )

}

/**
  * Created by jstabel on 4/1/16.
  * authorize
  * requires( Optional[List"Admin"] = None, Optional = None
  * withGroups(Optional["Admin"], Optional["Guest"])(withoutGroups(["Guest"])( ctx => {}))
  */
trait SecuritySupport {

  val securityContext:SecurityContext

  def authorize(requiredGroups:Seq[String], deniedGroups:Seq[String] = Seq.empty)(body:Unit) = {
    if (securityContext.hasAllGroups(requiredGroups) && securityContext.hasNoneOfGroups(deniedGroups)) {
      body
    }
  }

  def foo: Unit = {
    authorize(Seq("Admin")) {
      println("wow")
    }
  }
}
