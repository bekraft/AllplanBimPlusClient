package bimplus.dto.marshalling

import java.util.UUID

import bimplus.dto._
import bimplus.BaseTestSpec

import scala.reflect.runtime.universe._

class TextSerializationTests extends BaseTestSpec {

	import bimplus.dto.marshalling._

	"parseTo" should "1 parse as Int(1)" in {
		val result = parseTo[Int]("1")
		result.isSuccess shouldBe true
		result.get shouldEqual 1
		result.get.getClass shouldEqual classOf[Int]
	}

	"parseTo" should "1.0 parse as Double(1.0)" in {
		val result = parseTo[Double]("1.0")
		result.isSuccess shouldBe true
		result.get shouldEqual 1.0
		result.get.getClass shouldEqual classOf[Double]
	}

	"parseTo" should "2352c33e-2e7c-44ae-8176-009feae79a3e parse as UUID()" in {
		val result = parseTo[UUID]("2352c33e-2e7c-44ae-8176-009feae79a3e")
		result.isSuccess shouldBe true
		result.get shouldEqual UUID.fromString("2352c33e-2e7c-44ae-8176-009feae79a3e")
		result.get.getClass shouldEqual classOf[UUID]
	}

	"fromText" should s"""Option(\"123\") as Option""" in {
		val result = TextMarshaller.fromText("2352c33e-2e7c-44ae-8176-009feae79a3e", typeOf[Option[UUID]])
		result.isSuccess shouldBe true
		result.get.getClass shouldEqual classOf[Some[UUID]]
		result.get.asInstanceOf[Option[_]].get shouldEqual UUID.fromString("2352c33e-2e7c-44ae-8176-009feae79a3e")
	}

	"fromText" should s"""BooleanInts.True as Enumeration""" in {
		// TODO createEnum(typeOf[BooleanInts.Type], "True")
	}

	"Serializing to text" should "Attribute serializes and deserializes correctly" in {
		// TODO Serializing to text
		// val a = Attribute.create("2352c33e-2e7c-44ae-8176-009feae79a3e", "Fixture Special weight")
	}
}
