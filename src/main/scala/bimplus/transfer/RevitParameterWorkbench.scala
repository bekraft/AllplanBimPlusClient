package bimplus.transfer

import java.io.File
import java.util.UUID

import akka.event.Logging

import bimplus.Api.system
import bimplus.dto._
import bimplus.{AttributeMatch, AttributeMatchStrategy}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.{Codec, Source}
import scala.util.{Failure, Success, Try}

/**
 * Revit paramter group
 * @param id The ID
 * @param name The name
 */
final case class RevitParamGroup(id: Int,
								 name: String) {

	def toAttributeGroup: AttributeGroup = AttributeGroup(None, Some(name))
}

/**
 * Revit parameter definition
 * @param guid The GUID
 * @param name The name
 * @param datatype The datatype
 * @param group The group
 * @param isVisible Visible Yes/No
 * @param isModifiable Modifiable Yes/No
 */
final case class RevitParam(guid: UUID,
							name: String,
							datatype: AttributeTypes.Type, // TODO Use Revit specific data types
							group: RevitParamGroup,
							isVisible: BooleanInts.Type,
							isModifiable: BooleanInts.Type) {

	def toAttribute: Attribute = {
		// TODO Data type matching for control types
		Attribute(None, None, Some(name), None, Some(datatype), Some(AttributeControlTypes.TextBox), None, None, Some(isVisible), Some(isModifiable))
	}
}

/**
 * Revit Parameter Workbench reading parameter definitions from shared configuration file (text-encoded, UTF-8)
 * @param txtFile The workbench file
 */
class RevitParameterWorkbench(val txtFile: Workbenchfile) {

	val log = Logging(system, getClass)

	/** Available codecs */
	val codecTrialOrder = List(Codec.UTF8, Codec.ISO8859)

	// Apply codecs and fall back to next if read fails
	private def recursiveTryCodecRead(wbf: Workbenchfile, codecs:Seq[Codec]): Try[(Seq[String], Codec)] = {
		codecs match {
			case Nil =>
				log.error(s"""No appropriate codec has been found!""")
				Failure(new IllegalArgumentException(s"""No appropriate codec has been found for '${wbf.resourceName}'"""))
			case _ =>
				wbf.inputStream.apply().map { in =>
					Try(Source.fromInputStream(in)(codecs.head).getLines.toSeq) match {
						case Success(readLines) =>
							Success((readLines, codecs.head))
						case Failure(exception) =>
							log.warning(s"""${codecs.head}: Read attempt failed on '${txtFile.resourceName}' with $exception""")
							recursiveTryCodecRead(wbf, codecs.tail)
					}
				}.flatten
		}
	}

	// Read lines and re-encode as UTF-8
	private lazy val readLines : Try[Seq[String]] = recursiveTryCodecRead(txtFile, codecTrialOrder).map {
		case (s: Seq[String], c: Codec) =>
			s.map { l => new String(l.getBytes(c.charSet), "UTF-8") }
	}

	/**
	 * Will read the parameter file and extract parameter information.
	 */
	lazy val (meta, groups, params) : (Option[String], Seq[RevitParamGroup], Seq[RevitParam]) = {
		val groups = new mutable.HashMap[Int, RevitParamGroup]
		var meta : Option[String] = None
		val params = new ListBuffer[RevitParam]
		val lines = readLines match {
			case Success(rawLines) => rawLines
			case Failure(exception) => throw exception
		}

		for (line <- lines) {
			val fields = line.stripLeading().split('\t')
			if (fields.length > 0) {
				fields(0) match {
					case "META" =>
						meta = Some(s"${fields(1)}.${fields(2)}")
					case "GROUP" =>
						val id = fields(1).toInt
						groups += (id -> RevitParamGroup(id, fields(2)))
					case "PARAM" =>
						val paramType = fields(3) match {
							// TODO Use Revit specifc datatype enumeration
							case "TEXT" => AttributeTypes.String
							case "INTEGER" => AttributeTypes.Int
							case "NUMBER" => AttributeTypes.Double
							case "YES/NO" => AttributeTypes.Boolean
							case "LENGTH" | "AREA" | "VOLUME" | "ANGLE" | "SLOPE" | "URL" | "MATERIAL" => AttributeTypes.String
							case _ => throw new NotImplementedError(s"""Missing implementation ${fields(3)}""")
						}
						params += RevitParam(
							UUID.fromString(fields(1)),
							fields(2),
							paramType,
							groups(fields(5).toInt),
							BooleanInts(fields(6).toInt),
							BooleanInts(fields(8).toInt))
					case _ =>
						log.debug(s"""Ignoring line '$line'""")
				}
			}
		}
		log.info(s"""Read file version: ${meta.get} with ${params.length} parameters (${txtFile.resourceName})""")
		(meta, groups.values.toSeq, params.toSeq)
	}

	/**
	 * Tries to match parameter definition against existing attributes and builds a template candidate
	 * by internal name or file name
	 * @param existingAttributes The existing BIM+ attributes
	 * @param attributeMatchStrategy The matching strategy
	 * @param elem The element types to bind against.
	 * @return A tuple of candidate template and left over parameters without matching
	 */
	def toAttributeTemplateCandidate(existingAttributes: Seq[Attribute],
									 attributeMatchStrategy: AttributeMatchStrategy,
									 elem: ElementType*): (AttributeTemplate, Seq[Attribute]) = {

		val matching = AttributeMatch(
			existingAttributes,
			params.map{_.toAttribute},
			attributeMatchStrategy)

		val template = AttributeTemplate(
			None,
			txtFile.name.orElse(Some(txtFile.resourceName)),
			None,
			None,
			Some(elem.toList),
			Some(matching.matchesLeft.map { Attribute.createRef }.toList)
		)
		(template, matching.rightOnly)
	}
}

object RevitParameterWorkbench {

	val extensions = Set(".txt")
	/** Reads a local Revit Shared Parameter file */
	def read(localFileName: String): RevitParameterWorkbench = new RevitParameterWorkbench(LocalWorkbenchfile(localFileName))

	/** Scans a folder for files by regular expressions to extract names */
	def scanFolderRegex(localFolderName: String, nameRegex: String, sep: String = " "): Seq[RevitParameterWorkbench] = {
		val folder = new File(localFolderName)
		if (folder.exists() && folder.isDirectory) {
			val files = folder.listFiles.filter { f => f.isFile && extensions.exists(f.getName.endsWith(_)) }
			val namePattern = nameRegex.r
			files.toSeq.map { file =>
			  	namePattern.findAllMatchIn(file.getName).toSeq match {
					case Nil =>
						println(s"""Skipping mismatch '${file.getName}'""")
						None
					case extractedNames =>
						Some(new RevitParameterWorkbench(
							LocalWorkbenchfile(file.getAbsolutePath, Some(extractedNames.map {_.matched.trim}.mkString(sep)))))
				}
			}.filter { _.isDefined}.map { _.get}
		} else {
			Seq[RevitParameterWorkbench]()
		}
	}

	/** Scans a folder for files and extract names by trimming off given sub strings  */
	def scanFolderTrim(localFolderName: String, toTrimAway: String*): Seq[RevitParameterWorkbench] = {
		val folder = new File(localFolderName)
		if (folder.exists() && folder.isDirectory) {
			val files = folder.listFiles.filter { f => f.isFile && extensions.exists(f.getName.endsWith(_)) }
			files.toSeq.map { file =>
				var name = file.getName
				for (toTrim <- toTrimAway ++ extensions)
					name = name.replaceAllLiterally(toTrim, "")
				new RevitParameterWorkbench(LocalWorkbenchfile(file.getAbsolutePath, Some(name.trim)))
			}
		} else {
			Seq[RevitParameterWorkbench]()
		}
	}
}
