package bimplus.dto

/** A "free" attribute group DTO according to the specs.
 * See [[https://doc.allplan.com/display/bimpluspublic/4.5+Custom+attribute+service#id-4.5Customattributeservice-createBimAttributeGroup BIMplus API]]
 *
 * @param id The DTO id
 * @param name The name
 * @param description The description
 * @param parent The parent group
 */
final case class AttributeGroup(id : Option[DtoId],
							   	name : Option[String] = None,
							   	description: Option[String] = None,
								parent : Option[DtoId] = None) extends DtoEntity {

	def filter(attribute: Seq[Attribute]): Seq[Attribute] = {
		id.map(guid => attribute.filter(_.parent.exists(_.equalsIgnoreCase(guid)))).getOrElse(Seq())
	}

	def asParent(attribute: Attribute*): Seq[Attribute] = {
		attribute.map {_.copy(parent = id)}
	}

	override def toString: String = s"""id: $id, name: $name"""
}

object AttributeGroup {
	/** Creates a group by given name */
	def create(name: String): AttributeGroup =
		AttributeGroup(
			None,
			Some(name)
		)

	/** Creates a group by complete specification */
	def create(name: String, description: Option[String], parent: Option[DtoId] = None): AttributeGroup =
		AttributeGroup(
			None,
			Some(name),
			description,
			parent
		)
}