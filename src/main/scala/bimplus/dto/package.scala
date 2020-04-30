package bimplus

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import bimplus.dto.marshalling.TextMarshaller
import spray.json.JsonWriter
import spray.json._

package object dto {
	/** The identifier type */
	type DtoId = String

	/** A simple DTO entity with an optional identifier */
	trait DtoEntity extends TextMarshaller {
		val id : Option[DtoId]
	}

	def printText[D <: DtoEntity](dto: D): Unit = {
		// TODO printText
	}

	/**
	 * Save entities to json text file
	 * @param fileName The file name
	 * @param dto The DTOs
	 * @param jsonWriter Implicit json writer codec
	 * @tparam D Type of DTO (if not implicitly given)
	 * @return Absolute file name
	 */
	def saveToFile[D <: DtoEntity](fileName: String, dto: D*)
								  (implicit jsonWriter: JsonWriter[List[D]]): String = {

		val path = Paths.get(fileName)
		Files.write(path, dto.toList.toJson.prettyPrint.getBytes(StandardCharsets.UTF_8))
		path.toAbsolutePath.toString
	}

	/**
	 * Reads DTOs from file
	 * @param fileName The file name to be read
	 * @param jsonReader The implicit reader codec
	 * @tparam D The type of DTO
	 * @return A list of DTOs
	 */
	def readFromFile[D <: DtoEntity](fileName: String)
								  	(implicit jsonReader: JsonReader[List[D]]): List[D] = {

		val path = Paths.get(fileName)
		Files.readString(path).parseJson.convertTo[List[D]]
	}
}
