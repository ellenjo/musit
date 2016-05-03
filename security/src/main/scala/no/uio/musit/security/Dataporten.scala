package no.uio.musit.security

import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by jstabel on 3/31/16.
  */


object dataporten {
  def createUserInfo(sub: String, name: String) = new UserInfo(sub, name)

  def createGroupInfo(groupType: String, id: String, displayName: String, description: Option[String]) = new GroupInfo(groupType, id, displayName, description)


  val userInfoUrl = "https://auth.dataporten.no/openid/userinfo"
  val userGroupsUrl = "https://groups-api.dataporten.no/groups/me/groups"

  implicit val userInfoReads: Reads[UserInfo] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "name").read[String]
    ) (createUserInfo _)

  implicit val groupInfoReads: Reads[GroupInfo] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "displayName").read[String] and
      (JsPath \ "description").readNullable[String]

    ) (createGroupInfo _)

  class DataportenUserInfoProvider(_accessToken: String) extends ConnectionInfoProvider {

    import no.uio.musit.microservices.common.extensions.PlayExtensions._

    def httpGet(url: String) = {
      WS.url(url).withBearerToken(_accessToken).getOrFail()
    }

    def getUserInfo = {
      httpGet(userInfoUrl).map(resp => resp.body).map(Json.parse(_).validate[UserInfo].get)
    }

    def getUserGroups = {
      httpGet(userGroupsUrl).map(resp => resp.body).map { j => /*println(j);*/ Json.parse(j).validate[Seq[GroupInfo]].get }
    }

    def accessToken = _accessToken
  }

  object Dataporten {
    def createSecurityConnection(accessToken: String, useCache: Boolean = true) = {
      val infoProvider = new DataportenUserInfoProvider(accessToken)
      Security.createSecurityConnectionFromInfoProvider(infoProvider, useCache)
    }
  }

}