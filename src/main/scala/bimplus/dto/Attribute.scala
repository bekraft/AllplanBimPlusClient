package bimplus.dto

import bimplus._

/** An attribute DTO structure according to specs.
 * See [[https://doc.allplan.com/display/bimpluspublic/4.5+Custom+attribute+service#bimCreateBimFreeAttribute--771079716 BIMplus API]]
 * @param id The ID
 * @param parent The group ID
 * @param name The name
 * @param description The description
 * @param `type` An attribute type of [[AttributeTypes]]
 * @param controltype An control type of [[AttributeControlTypes]]
 * @param defaultvalue The default value
 * @param enumDefinition The enum/checkbox definition
 * @param visible Whether the attribute is visible
 * @param changeable Whether the attribute is changeable
 */
final case class Attribute(id: Option[DtoId],
						   parent: Option[DtoId],
						   name: Option[String],
						   description: Option[String] = None,
						   `type`: Option[AttributeTypes.Type] = None,
						   controltype: Option[AttributeControlTypes.Type] = None,
						   defaultvalue: Option[String] = None,
						   // Provided by compliance
						   enumDefinition: Option[String] = None,
						   visible: Option[BooleanInts.Type] = None,
						   changeable: Option[BooleanInts.Type] = None,
						   symbol: Option[String] = None,
						   minValue: Option[String] = None,
						   maxValue: Option[String] = None) extends DtoEntity {

	override def toString: String = s"""id:$id, parent:$parent, name:$name"""
}

object Attribute {
	/** Creates a light weight reference only */
	def createRef(a:Attribute): Attribute = Attribute(a.id, None, None)

	/** Creates a new property by id and name */
	def create(id : DtoId, name: String): Attribute =
		Attribute(
			toBlankOption(id),
			None,
			Option(name)
		)

	/** Creates a new property */
	def create(group: DtoId, name: String, description: String, attributeType: AttributeTypes.Type, controlType: AttributeControlTypes.Type): Attribute =
		Attribute(
			None,
			Some(group),
			Option(name),
			Option(description),
			Option(attributeType),
			Option(controlType)
		)

	/** Creates a new text property */
	def createText(group: DtoId, name: String, description: String, defaultValue: Option[String] = None): Attribute =
		Attribute(
			None,
			Option(group),
			Option(name),
			Option(description),
			Some(AttributeTypes.String),
			Some(AttributeControlTypes.TextBox),
			defaultValue
		)
}