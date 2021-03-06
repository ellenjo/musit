package models.analysis.events

import no.uio.musit.formatters.WithDateTimeFormatters
import no.uio.musit.models.{ActorId, ObjectUUID}
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}

object SaveCommands {

  sealed trait SaveAnalysisEventCommand {
    def asDomain: AnalysisEvent
  }

  case class SaveAnalysis(
      analysisTypeId: AnalysisTypeId,
      eventDate: Option[DateTime],
      note: Option[String],
      objectId: ObjectUUID,
      // TODO: Add field for status
      responsible: Option[ActorId],
      administrator: Option[ActorId],
      updatedBy: Option[ActorId],
      updatedDate: Option[DateTime],
      completedBy: Option[ActorId],
      completedDate: Option[DateTime]
  ) extends SaveAnalysisEventCommand {

    override def asDomain: Analysis = {
      Analysis(
        id = None,
        analysisTypeId = analysisTypeId,
        eventDate = eventDate,
        registeredBy = None,
        registeredDate = None,
        objectId = Some(objectId),
        responsible = responsible,
        administrator = administrator,
        updatedBy = updatedBy,
        updatedDate = updatedDate,
        completedBy = completedBy,
        completedDate = completedDate,
        partOf = None,
        note = note,
        result = None
      )
    }

  }

  object SaveAnalysis extends WithDateTimeFormatters {

    implicit val reads: Reads[SaveAnalysis] = Json.reads[SaveAnalysis]

  }

  case class SaveAnalysisCollection(
      analysisTypeId: AnalysisTypeId,
      eventDate: Option[DateTime],
      note: Option[String],
      responsible: Option[ActorId],
      administrator: Option[ActorId],
      updatedBy: Option[ActorId],
      updatedDate: Option[DateTime],
      completedBy: Option[ActorId],
      completedDate: Option[DateTime],
      // TODO: Add field for status
      objectIds: Seq[ObjectUUID]
  ) extends SaveAnalysisEventCommand {

    override def asDomain: AnalysisCollection = {
      AnalysisCollection(
        id = None,
        analysisTypeId = this.analysisTypeId,
        eventDate = this.eventDate,
        registeredBy = None,
        registeredDate = None,
        events = this.objectIds.map { oid =>
          Analysis(
            id = None,
            analysisTypeId = this.analysisTypeId,
            eventDate = this.eventDate,
            registeredBy = None,
            registeredDate = None,
            objectId = Option(oid),
            responsible = responsible,
            administrator = administrator,
            updatedBy = updatedBy,
            updatedDate = updatedDate,
            completedBy = completedBy,
            completedDate = completedDate,
            partOf = None,
            note = this.note,
            result = None
          )
        }
      )
    }

  }

  object SaveAnalysisCollection extends WithDateTimeFormatters {

    implicit val reads: Reads[SaveAnalysisCollection] =
      Json.reads[SaveAnalysisCollection]

  }

}
