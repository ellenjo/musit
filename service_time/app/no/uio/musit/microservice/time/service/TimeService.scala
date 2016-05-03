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

package no.uio.musit.microservice.time.service

import no.uio.musit.microservice.time.domain._

trait TimeService {
  def getNow(filter:Option[MusitJodaFilter] = None): MusitTime = {
    val now = org.joda.time.DateTime.now
    filter match {
      case None => DateTime(Date(now.toLocalDate), Time(now.toLocalTime))
      case Some(f:MusitDateTimeFilter) => DateTime(Date(now.toLocalDate), Time(now.toLocalTime))
      case Some(f:MusitDateFilter) => Date(now.toLocalDate)
      case Some(f:MusitTimeFilter) => Time(now.toLocalTime)
      case _ => throw new IllegalArgumentException("Not supported filter type")
    }
  }

}
