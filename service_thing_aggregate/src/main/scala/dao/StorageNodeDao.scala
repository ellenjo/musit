package dao

import com.google.inject.Inject
import models.MusitResults.{ MusitDbError, MusitResult, MusitSuccess }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

import scala.concurrent.Future

class StorageNodeDao @Inject() (
    val dbConfigProvider: DatabaseConfigProvider
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  def nodeExists(nodeId: Long): Future[MusitResult[Boolean]] = {
    db.run(
      sql"""
         select count(*)
         from MUSARK_STORAGE.STORAGE_NODE
         WHERE storage_node_id = $nodeId
      """.as[Long].head.map(res => MusitSuccess(res == 1))
    ).recover {
        case e: Exception => MusitDbError(s"Error occurred while checking for node existence for nodeId $nodeId", Some(e))
      }
  }
}