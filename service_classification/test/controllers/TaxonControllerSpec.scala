package controllers

import models.ScientificName
import no.uio.musit.MusitResults.{MusitInternalError, MusitValidationError}
import no.uio.musit.security.BearerToken
import no.uio.musit.test.{FakeUsers, MusitSpecWithServerPerSuite}
import play.api.http.Status
import play.api.libs.json.JsArray
import org.scalatest.Inspectors._
import play.api.test.Helpers._


class TaxonControllerSpec
  extends MusitSpecWithServerPerSuite {

    val searchByTerm = (scientName: String) => s"/v1/taxon?search=$scientName"
    val searchBySuggestedTerm = (suggestname: String) => s"/v1/taxon/scientificName/suggest?suggest=$suggestname"
    val getScientificNameById = (scientificNameId: Long) => s"/v1/taxon/scientificName/$scientificNameId"
    val getTaxonById =(id: Long) => s"/v1/taxon/$id"

    val fakeToken = BearerToken(FakeUsers.testUserToken)

    "Using the ArtDataBanken API" when {
      "searching for scientificName" should {
        "return a spesicfic taxon matching the query parameter" in {
          val res = wsUrl(searchByTerm("alopex lagopus"))
            .withHeaders(fakeToken.asHeader)
            .get().futureValue

          res.status mustBe Status.OK

          val jsArr = res.json.as[JsArray].value
          jsArr must not be empty
          jsArr.size mustBe 1
          (jsArr.head \ "taxonId").as[Int] mustBe 83770
          (jsArr.head \ "id").as[String] mustBe "Taxons/83770"
        }
        "return no content if no match of query parameter" in {
          val res = wsUrl(searchByTerm("alopex lagopusssssssssssss"))
            .withHeaders(fakeToken.asHeader)
            .get().futureValue

          res.status mustBe Status.NO_CONTENT
        }
        "return a list of matching names of the query parameter" in {
          val res = wsUrl(searchBySuggestedTerm("alopes lagopos"))
            .withHeaders(fakeToken.asHeader)
            .get().futureValue

          res.status mustBe Status.OK

          val jsArr = res.json.as[JsArray].value
          jsArr must not be empty
          jsArr.size mustBe 4
          jsArr.head.as[String] mustBe "alopex lagopus"
        }
        "return no content if no match of suggested query parameter" in {
          val res = wsUrl(searchBySuggestedTerm("aaaaalopex lagopusssssssssssss"))
            .withHeaders(fakeToken.asHeader)
            .get().futureValue

          res.status mustBe Status.NO_CONTENT
        }

        "return the scientificName from a given scientificNameId" in {
          val res = wsUrl(getScientificNameById(126845))
            .withHeaders(fakeToken.asHeader)
            .get().futureValue

          res.status mustBe Status.OK
          (res.json \ "taxonId").as[Int] mustBe 83770
          val accName = (res.json \ "acceptedNameUsage" \ "id").asOpt[String] mustBe Some("")
          val hc = (res.json \ "higherClassification").asOpt[JsArray]
          hc must not be None
          hc.value.value.size mustBe 8

          val sciNames = hc.value.value.map(js => (js \ "scientificName").as[String])
          sciNames must contain allOf ("Animalia", "Chordata", "Vertebrata", "Mammalia")

          val strStatuses = hc.value.value.map(js => (js \ "taxonomicStatus").as[String])
          strStatuses must contain inOrderOnly ("accepted", "heterotypic synonym")

          forAll(hc.value.value) { js =>
            (js \ "id").as[String] must startWith("ScientificNames/")
          }

          val dc = (res.json \ "dynamicProperties").asOpt[JsArray]
          dc must not be None
          dc.value.value.size mustBe 2

          val dcValues = dc.value.value.map(js => (js \ "value").as[String])
          dcValues must contain allOf ("Pattedyr (Norge)", "Pattedyr")

          forAll(dc.value.value) { js =>
            (js \ "name").as[String] mustBe "GruppeNavn"
          }

          val dcProp = dc.value.value.map(js => (js \ "properties").asOpt[JsArray])
          dcProp must not be None
          dcProp.map { jsprop =>
            jsprop.value.value.size mustBe 2
            val jsPropValues = jsprop.value.value.map(js => (js \ "value").as[String])
            jsPropValues must contain("126845")
          }
        }
        "return the Bad Request from a negative scientificNameId " in {
          val res = wsUrl(getScientificNameById(-1))
            .withHeaders(fakeToken.asHeader)
            .get().futureValue

          res.status mustBe Status.BAD_REQUEST
          res.body.contains("Id must be a positive integer")

        }

      "return Bad request from a too long scientificNameId" in {
        val res = wsUrl(getScientificNameById(Long.MaxValue))
          .withHeaders(fakeToken.asHeader)
          .get().futureValue
        res.status mustBe Status.BAD_REQUEST
      }

       "return Bad request from a negative taxonId" in {
         val res = wsUrl(getTaxonById(-1))
           .withHeaders(fakeToken.asHeader)
           .get().futureValue
         res.status mustBe Status.BAD_REQUEST
         res.body.contains("Id must be a positive integer")
        }

        "return Bad request from a too long taxonId" in {
          val res = wsUrl(getTaxonById(Long.MaxValue))
            .withHeaders(fakeToken.asHeader)
            .get().futureValue
          res.status mustBe Status.BAD_REQUEST

        }

        "return taxon from a given taxonId" in {
          val res = wsUrl(getTaxonById(83770))
            .withHeaders(fakeToken.asHeader)
            .get().futureValue

          res.status mustBe Status.OK
          (res.json \ "taxonId").as[Int] mustBe 83770
          val sn = (res.json \ "scientificNames").asOpt[JsArray]
          sn must not be None
          sn.value.value.size mustBe 2

          val vn = (res.json \ "vernacularNames").asOpt[JsArray]
          vn must not be None
          vn.value.value.size mustBe 4
          val vNames = vn.value.value.map(js => (js \ "vernacularName").as[String])
          forAll(vNames) { rev =>
            rev must include ("rev")
          }
          val pvn = (res.json \ "preferredVernacularName" \ "nomenclaturalStatus").asOpt[String]
          pvn.value mustBe ("preferred")

          val dp = (res.json \ "dynamicProperties").asOpt[JsArray]
          dp must not be None
          dp.value.value.size mustBe 1


        }
      }
    }

}
