package bimplus.transfer

import java.io.{FileInputStream, FileOutputStream, InputStream, OutputStream}

import scala.util.{Failure, Try}

/** Workbench file abstraction */
trait Workbenchfile {
	val resourceName: String
	val name: Option[String]
	val overwrite: Boolean
	def inputStream: () => Try[InputStream]
	def outputStream: () => Try[OutputStream]
}

/**
 * Local file support loading from file system
 * @param resourceName The file name
 * @param name The optional name
 * @param overwrite Indicates whether a file is able to write
 */
case class LocalWorkbenchfile(resourceName: String,
							  name: Option[String] = None,
							  overwrite: Boolean = false) extends Workbenchfile {

	override def inputStream: () => Try[InputStream] = () =>
		Try(new FileInputStream(resourceName))


	override def outputStream: () => Try[OutputStream] = () =>
		if (overwrite)
			Try(new FileOutputStream(resourceName))
		else
			Failure(new IllegalAccessException(s"""Not allowed: Overwrite=$overwrite for '$resourceName''"""))
}

/**
 * Input resource loading support loading from class path
 * @param resourceName The resource name
 */
case class ResourceWorkbenchfile(resourceName: String) extends Workbenchfile {

	override val overwrite: Boolean = false
	override val name: Option[String] = Some(resourceName)

	override def inputStream: () => Try[InputStream] = () =>
		Try(getClass.getResourceAsStream(resourceName))

	override def outputStream: () => Try[OutputStream] = () =>
	  	Failure(new IllegalStateException(s"""Not available for class path resource '$resourceName'"""))
}
