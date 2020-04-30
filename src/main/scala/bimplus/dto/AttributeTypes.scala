package bimplus.dto

/**
 * Attribute type enumeration.
 */
object AttributeTypes extends Enumeration {
	type Type = Value

	val String = Value("string")
	val Int = Value("int")
	val Double = Value("double")
	val Guid = Value("guid")
	val Datetime = Value("datetime")
	val Binary = Value("binary")
	val Boolean = Value("boolean")
}
