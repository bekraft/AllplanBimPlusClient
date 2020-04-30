package bimplus

import bimplus.dto._
import bimplus.DtoResult._
import bimplus.services._
import bimplus.transfer.{ExcelWorkbench, ResourceWorkbenchfile}
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.util.ConfigLoader

import scala.concurrent.Await
import scala.concurrent.duration._

class ApiStageServiceTests extends BaseTestSpec {

	lazy val fixtureWorkbench = new ExcelWorkbench(ResourceWorkbenchfile("/ExampleAttributes.xls"))
	lazy val api = Api.stage

	val config = ConfigFactory.load("local.application.test.conf")

	var fixtureGroup : Option[AttributeGroup] = None
	var fixtureAttributes : List[Attribute] = Nil
	var fixtureProjectTemplate : Option[AttributeProjectTemplate] = None
	var fixtureTemplate : Option[AttributeTemplate] = None

	"Stage login" should "be successful" in {
		val stageLogin = config.getString("stage-login")
		val stagePwd = config.getString("stage-pwd")
		val stageTeamslug = config.getString("stage-teamslug")
		val grant = api.login(stageLogin, stagePwd)
		Api.applicationContext.teamslug = Some(stageTeamslug)

		grant.isDefined shouldBe true
		Api.applicationContext.authorization.isDefined shouldBe true
	}

	"Stage attribute group import" should "be successful" in {
		Api.applicationContext.authorization.isDefined shouldBe true
		fixtureGroup = Await.result(api.attributeGroups.create("Test group"), api.timeOut).unboxed

		fixtureGroup.isDefined shouldBe true
		log.info(s"""${fixtureGroup.get}""")
	}

	"Stage attributes import" should "be successful" in {
		Api.applicationContext.authorization.isDefined shouldBe true
		fixtureGroup.isDefined shouldBe true

		// Ignore first - header
		val fixtures = fixtureWorkbench.fixedFormatReadAttribute("Attributes", 1)
		fixtures.length shouldEqual 2

		implicit val onFailing: OnFailing[Attribute] = newOnFailingDefault
		fixtureAttributes = Await.result(api.attributes.createMany(fixtureGroup.get.asParent(fixtures:_ *)), api.timeOut).toList

		fixtureAttributes.length shouldBe 2
	}

	"Stage attribute template generation" should "be successful" in {
		Api.applicationContext.authorization.isDefined shouldBe true
		fixtureAttributes.length shouldBe 2

		fixtureProjectTemplate = Await.result(api.attributeProjectTemplates.create("Test project template"), api.timeOut).unboxed
		fixtureProjectTemplate.isDefined shouldBe true

		val result = Await.result(
			api.createTemplateFromGroupReference(
				fixtureProjectTemplate.get, fixtureAttributes, ElementType.buildingElementType), api.timeOut).values.filter {_.isDefined}.toList

		result.length shouldBe 1
		fixtureTemplate = result(0)
	}

	"Stage template removal" should "be successful" in {
		Api.applicationContext.authorization.isDefined shouldBe true
		fixtureProjectTemplate.isDefined shouldBe true

		Await.result(api.attributeProjectTemplates.delete(fixtureProjectTemplate.get), api.timeOut).unboxed.isDefined shouldBe true
	}

	"Stage group removal" should "be successful" in {
		Api.applicationContext.authorization.isDefined shouldBe true
		fixtureGroup.isDefined shouldBe true

		implicit val onFailing: OnFailing[Attribute] = newOnFailingDefault
		Await.result(api.attributeGroups.delete(fixtureGroup.get, true), api.timeOut).unboxed.isDefined shouldBe true
	}

}
