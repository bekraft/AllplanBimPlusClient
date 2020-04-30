package bimplus.dto.marshalling

import scala.reflect.runtime.universe._

/**
 * A column tag binding column index and method symbol of a type
 * @param column The work sheet column
 * @param symbol The method symbol mapped to this column
 */
case class ColumnTag(symbol: MethodSymbol, column: Int)

object ColumnTag {
	import TextMarshaller._

	/** Create columns tags from DTO type only */
	def create[T](implicit tag:TypeTag[T]): List[ColumnTag] = {
		val indexed: Seq[(MethodSymbol, Int)] = caseAccessors[T].zipWithIndex
		indexed.map { t => ColumnTag.apply(t._1, t._2) }.toList
	}

	/** Create column tags from given header and DTO type */
	def create[T](header: Seq[(String, Int)], ignoreCase: Boolean = true)
				 (implicit tag:TypeTag[T]): List[ColumnTag] = {
		val tags = create[T]
		header.map { column =>
			tags.find { t =>
				if (ignoreCase)
					t.symbol.name.toString.equalsIgnoreCase(column._1)
				else
					t.symbol.name.toString.equals(column._1)
			}
			.map { t =>
				ColumnTag(t.symbol, column._2)
			}
		}
		.filter { _.isDefined }
		.map { _.get }
		.toList
	}
}