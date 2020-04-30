package bimplus.dto.marshalling

import java.util.UUID

import scala.Enumeration
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import bimplus.dto.marshalling._

trait TextMarshaller {

	def caseFields : List[(String, Int)] = this.productElementNames.zipWithIndex.toList

	def toText(productNames: Seq[String] = Nil): Seq[Option[String]] = {
		val header = productNames match {
			case Nil => caseFields.map { _._1 }
			case _ => productNames
		}

		for (n <- header) yield {
			caseFields
				.find { f => f._1.equalsIgnoreCase(n) }
    			.map { f => TextMarshaller.toText(this.productElement(f._2)) }
		}
	}
}

object TextMarshaller {

	def caseAccessors[T](implicit tag:TypeTag[T]): List[MethodSymbol] = {
		typeOf[T].members.collect {
			case m: MethodSymbol if m.isCaseAccessor => m
		}.toList
	}

	def caseValues[T](obj: T, symbols: Seq[MethodSymbol])
					 (implicit rm: RuntimeMirror, classTag: ClassTag[T]): Seq[String] = {
		val im = rm.reflect(obj)
		symbols.map{ m => im.reflectField(m).get match {
			case Some(value) =>
				value.toString
			case None =>
				""
			case value:Any =>
				value.toString
		} }
	}

	def caseTypes[T](productNames: Seq[String])(implicit tag:TypeTag[T]): Seq[Option[Type]] = {
		val accessors = caseAccessors[T]
		for (n <- productNames) yield {
			accessors
				.find { a => a.name.toString.equalsIgnoreCase(n) }
				.map { a => a.returnType }
		}
	}

	def toText(data: Any): String = {
		data match {
			case (s: String) =>
				s
			case (i: Int) =>
				i.toString
			case (d: Double) =>
				d.toString
			case (n: Number) =>
				n.toString
			case (b: Boolean) =>
				if (b) "true" else "false"
			case (g: UUID) =>
				g.toString
			case _ =>
				Option(data).map { _.toString }.getOrElse("")
		}
	}

	def fromText(str: String, toType: Type): Try[Any] = {
		toType match {
			case x if x <:< typeOf[Option[_]] =>
				fromText(str, toType.typeArgs.head) match {
					// Wrap result into option
					case Success(data) => Success(Some(data))
					case Failure(e) => Failure(e)
				}
			case x if x <:< typeOf[String] =>
				Try(str)
			/*case x if x <:< typeOf[Enumeration#Value] =>
				toType.getClass.getMethods.find {
					m => m.getName == "withName"
				} match {
					case None =>
						Failure(new Exception("Missing \"withName\" method for enumeration."))
					case Some(m) =>
						Try("Tst")//m.invoke(, str).asInstanceOf[N]) // TODO
				}
			*/
			case x if x <:< typeOf[Int] =>
				parseTo[Int](str)
			case x if x <:< typeOf[Double] =>
				parseTo[Double](str)
			case x if x <:< typeOf[UUID] =>
				parseTo[UUID](str)
			case _ =>
				Failure[Any](new Exception("Unhandled text unmarshalling"))
		}
	}

/*
	def parseTo[T](productNames: Seq[String], values: Seq[String])(implicit tag:TypeTag[T]): Try[T] = {

	}
*/
}
