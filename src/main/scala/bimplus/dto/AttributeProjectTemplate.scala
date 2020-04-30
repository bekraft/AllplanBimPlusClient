package bimplus.dto

/**
 * Project attribute set template
 * @param id The ID
 * @param name The name
 * @param description The description
 */
final case class AttributeProjectTemplate(id : Option[DtoId],
										  name : Option[String],
										  description: Option[String] = None) extends DtoEntity {

	override def toString: DtoId = s"""id: $id, name: $name"""

	/** Applies this project template to given attribute templates */
	def templateOf(attributeTemplate: AttributeTemplate*): Seq[AttributeTemplate] = attributeTemplate.map(_.copy(projectAttributeTemplate = id))
}

object AttributeProjectTemplate {
	/** Creates a new project template */
	def create(name : String, description: Option[String] = None ): AttributeProjectTemplate =
		AttributeProjectTemplate(
			None,
			Some(name),
			description
		)
}