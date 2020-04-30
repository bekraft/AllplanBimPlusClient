package bimplus

import bimplus.dto.{Attribute, DtoId}

trait AttributeMatchStrategy {
	def keyExtractor: (Attribute => String)
}

/** Name equality matcher ignoring case */
case class IgnoreCaseMatchStrategy(defaultName: String = "") extends AttributeMatchStrategy {
	override def keyExtractor: Attribute => String = (a => a.name.map{ _.toLowerCase}.getOrElse(defaultName))
}

/** Name equality matcher using case */
case class CaseMatchStrategy(defaultName: String = "") extends AttributeMatchStrategy {
	override def keyExtractor: Attribute => String = (a => a.name.getOrElse(defaultName))
}

/** ID equality */
case class DtoIdMatchStrategy(defaultId: DtoId) extends AttributeMatchStrategy {
	override def keyExtractor: Attribute => String = (a => a.id.map{_.toString}.getOrElse(defaultId.toString))
}

/**
 * An attribute matching based on two-way diffing between left and right attribute sets
 *
 * @param left     The left hand set
 * @param right    The right hand set
 * @param strategy The equality strategy (i.e. [[IgnoreCaseMatchStrategy]], [[CaseMatchStrategy]] or [[DtoIdMatchStrategy]])
 */
case class AttributeMatch(left: Seq[Attribute], right: Seq[Attribute], strategy: AttributeMatchStrategy) {

	lazy val leftMap : Map[String, Attribute] = left.map { a => strategy.keyExtractor(a) -> a}.toMap
	lazy val rightMap : Map[String, Attribute] = right.map { a => strategy.keyExtractor(a) -> a}.toMap

	def isMatch: Boolean = leftMap.keys
	  .forall(rightMap.contains) && rightMap.keys.forall(leftMap.contains)

	def matchesRight: Seq[Attribute] = leftMap.keys
	  .filter {rightMap.contains}
	  .map { rightMap(_) }.toSeq

	def matchesLeft: Seq[Attribute] = rightMap.keys
	  .filter {leftMap.contains}
	  .map { leftMap(_) }.toSeq

	def leftOnly: Seq[Attribute] = leftMap.keys
	  .filter { !rightMap.contains(_) }
	  .map {leftMap(_)}.toSeq

	def rightOnly: Seq[Attribute] = rightMap.keys
	  .filter { !leftMap.contains(_) }
	  .map {rightMap(_)}.toSeq
}
