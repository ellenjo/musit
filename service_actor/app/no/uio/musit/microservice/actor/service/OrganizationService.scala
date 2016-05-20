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
package no.uio.musit.microservice.actor.service

import no.uio.musit.microservice.actor.dao.ActorDao
import no.uio.musit.microservice.actor.domain.Organization
import no.uio.musit.microservices.common.linking.domain.Link

import scala.concurrent.Future

/**
  * Business logic for the Person entity in the microservice, simple lookups and so on.
  */
trait OrganizationService {

  def all = {
    ActorDao.allOrganizations()
  }

  def find(id:Long):Future[Option[Organization]] = {
    Future.successful(Some(Organization(id, "Find", "", "", "", 0, 0, Seq.empty[Link])))
  }

  def create(organization:Organization):Future[Option[Organization]] = {
    Future.successful(Some(organization))
  }

  def update(organization:Organization):Future[Option[Organization]] = {
    Future.successful(Some(organization))
  }

  def remove(id:Long):Future[Option[Organization]] = {
    Future.successful(Some(Organization(id, "Remove", "", "", "", 0, 0, Seq.empty[Link])))
  }


}
