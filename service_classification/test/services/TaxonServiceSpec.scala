package services

import no.uio.musit.test.MusitSpecWithAppPerSuite

class TaxonServiceSpec extends MusitSpecWithAppPerSuite {

  val service: TaxonService = fromInstanceCache[TaxonService]

  "TaxonService" when {
    "searching for taxons and scientificNames" should {

      "return a list of suggested scientificNames" in {
        val namesRes = service.searchScientNameSuggest("alopes lagopos").futureValue
        //println(namesRes.get)
        namesRes.isSuccess mustBe true
        val names = namesRes.get
        names.size mustBe 4
        names.headOption mustBe Some("alopex lagopus")

      }

      "return the Taxon of a spesific scientificName" in {
        val scientNamesRes = service.searchByTerm("alopex lagopus").futureValue
        //println(s" println: ${scientNamesRes.get.head}")
        scientNamesRes.isSuccess mustBe true
        val scientName = scientNamesRes.get
        scientName.size mustBe 1
        scientName.head.taxonId mustBe 83770
      }

      "return the scientificName from a given scientificNameId" in {
        val idRes = service.getScientificNameById(126845).futureValue
        //println(s" println: ${idres.get}")
        idRes.isSuccess mustBe true
        idRes.get.nonEmpty mustBe true
        idRes.get.get.scientificName mustBe "Alopex lagopus"
        idRes.get.get.taxonId mustBe 83770
        idRes.get.get.higherClassification must not be empty
      }
      "return the 404 Not Found from a negative scientificNameId" in {
        val idNegRes = service.getScientificNameById(-1).futureValue
        idNegRes.isSuccess mustBe true
        idNegRes.get.isEmpty mustBe true
      }

      "return taxon from a given taxonId" in {
        val taxonIdRes = service.getTaxonById(83770).futureValue
        taxonIdRes.isSuccess mustBe true
        taxonIdRes.get.nonEmpty mustBe true
        taxonIdRes.get.get.scientificNames must not be empty
      }

      "return InternalServerError from a negative taxonId" in {
        val taxonNegRes = service.getTaxonById(-1).futureValue
        taxonNegRes.isFailure mustBe true

      }
    }
  }
}
