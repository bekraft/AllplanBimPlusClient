package bimplus.dto

/**
 * An attribute template associated with element types.
 * See [[https://doc.allplan.com/display/bimpluspublic/4.5+Custom+attribute+service#id-4.5Customattributeservice-createTeamProjectAttributeTemplate BIM+ API]]
 * @param id The ID
 * @param name The name
 * @param description The description
 * @param projectAttributeTemplate The project attribute template
 * @param elementtypes Associated element types
 * @param freeattributes Template attributes
 * @param groups Template attribute groups
 * @param visible Whether visible
 * @param changeable Whether changeable
 */
final case class AttributeTemplate(id : Option[DtoId],
								   name : Option[String],
								   description: Option[String] = None,
								   projectAttributeTemplate: Option[DtoId] = None,
								   elementtypes: Option[List[ElementType]] = Nil,
								   freeattributes: Option[List[Attribute]] = Nil,
								   groups: Option[List[AttributeGroup]] = Nil,
								   visible: Option[BooleanInts.Type] = Some(BooleanInts.True),
								   changeable: Option[BooleanInts.Type] = Some(BooleanInts.True),
								   orderNumber: Option[Int] = None) extends DtoEntity {

	override def toString: String = s"""id: $id, name: $name"""

	/** Apply this template to given groups */
	def templateOfGroups(attributeGroup: Seq[AttributeGroup]): AttributeTemplate = copy(groups = attributeGroup.toList)
	/** Apply ths template to given attributes */
	def templateOfAttributes(attribute: Seq[Attribute]): AttributeTemplate = copy(freeattributes = attribute.toList)
}

object AttributeTemplate {
	/** Create a new template by name and optionally with given description */
	def create(prjAttributeTemplate: DtoId, name: String, description: Option[String] = None): AttributeTemplate =
		AttributeTemplate(
			None,
			Option(name),
			description,
			Some(prjAttributeTemplate)
		)

	def create(prjAttributeTemplate: DtoId, name: String, attributes: Seq[Attribute], elementType: ElementType*): AttributeTemplate = {
		val elementTypeList = elementType match {
			case Nil => None
			case _ => Some(elementType.toList)
		}
		// Build shallow attribute clones (only ID is required)
		val attrList = attributes match {
			case Nil => None
			case _ => Some(attributes.map {a => Attribute(a.id, None, None)}.toList)
		}

		AttributeTemplate(
			None,
			Option(name),
			None,
			Some(prjAttributeTemplate),
			elementTypeList,
			attrList
		)
	}
}