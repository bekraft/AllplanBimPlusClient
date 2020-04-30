package bimplus.dto

/**
 * Boolean Int enumeration using using "1" for true and "0" for false.
 */
object BooleanInts extends Enumeration {
	type Type = Value

	val False = Value(0, "0")
	val True = Value(1, "1")
}
