package bimplus.dto

/**
 * An element type DTO
 * @param id The ID
 * @param name The name
 * @param `type` The internal type
 * @param category The internal topology
 * @param layer The model layer
 */
final case class ElementType(id : Option[DtoId],
							 name : String,
							 `type` : Option[String] = None,
							 category : Option[String] = None,
							 layer : Option[String] = None) extends DtoEntity {

	override def toString: String = s"""id: $id, name: $name"""
}

object ElementType {
	/** General building element type */
	val buildingElementType : ElementType = ElementType(Some("4ba2f80c-a4d7-4d21-9fed-517f15467f24"), "ID_BuildingElement")
	val geometryObjectType : ElementType = ElementType(Some("5a8a8670-cb9f-4f04-a467-04a9e416a6d0"), "ID_Arch3DObject")
}