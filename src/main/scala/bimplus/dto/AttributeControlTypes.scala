package bimplus.dto

/**
 * Attribute control type enumeration.
 */
object AttributeControlTypes extends Enumeration {
	type Type = Value

	val Checkbox = Value("checkbox")
	val Datepicker = Value("datepicker")
	val TextBox = Value("textbox")
	val Enumeration = Value("enumeration")
	val Combobox = Value("combobox")
}
