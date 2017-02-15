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
        val  idres = service.getScientificNameById(126845).futureValue
        //println(s" println: ${idres.get}")
        idres.isSuccess mustBe true
        idres.get.nonEmpty mustBe true
        idres.get.get.scientificName mustBe Some("Alopex lagopus")
        idres.get.get.taxonId mustBe 83770
        idres.get.get.higherClassification must not be empty
      }

    }
  }
}
