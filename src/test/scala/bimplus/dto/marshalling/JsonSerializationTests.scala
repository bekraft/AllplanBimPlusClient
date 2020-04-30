package bimplus.dto.marshalling

import bimplus.BaseTestSpec
import bimplus.dto._
import spray.json._

class JsonSerializationTests extends BaseTestSpec with JsonMessageMarshaller {

	val attributeShort = """{
					  |    "visible": 1,
					  |    "id": "2352c33e-2e7c-44ae-8176-009feae79a3e",
					  |    "name": "Fixture Special weight"
					  |}""".stripMargin

	val attributeFull = """"""

	val attributeTemplateShort = """{
								   |        "name": "Column",
								   |        "description": "Properties for IfcColumn element.",
								   |        "created": "2019-08-09T05:57:04.407",
								   |        "createdby": {
								   |            "email": null
								   |        },
								   |        "changed": "2020-02-27T07:38:36.897",
								   |        "changedby": {
								   |            "email": null
								   |        },
								   |        "orderNumber": 10004,
								   |        "projectAttributeTemplate": "0149c4a8-4e84-4c40-bfdd-5dd63b109266",
								   |        "visible": 1,
								   |        "changeable": 0,
								   |        "id": "89d7e9ac-2fb2-c942-e78f-04049f6874f3"
								   |}""".stripMargin

	val attributeTemplateFull = """"""

	"Attribute" should "be deserialized and serialized" in {
		val fixture = attributeShort.parseJson.convertTo[Attribute]
		fixture.toJson

		fixture.visible shouldEqual Some(BooleanInts.True)
		fixture.id shouldEqual Some("2352c33e-2e7c-44ae-8176-009feae79a3e")
		fixture.name shouldEqual Some("Fixture Special weight")
	}

	"AttributeTemplate" should "be deserialized and serialized" in {
		val fixture = attributeTemplateShort.parseJson.convertTo[AttributeTemplate]
		fixture.id shouldEqual Some("89d7e9ac-2fb2-c942-e78f-04049f6874f3")
		fixture.toJson.compactPrint.isEmpty shouldBe false
	}
}
