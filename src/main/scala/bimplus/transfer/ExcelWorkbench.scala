package bimplus.transfer

import java.io.FileOutputStream

import akka.event.Logging
import bimplus.Api.system
import bimplus.dto._
import bimplus.dto.marshalling.{TextMarshaller, _}
import bimplus.toBlankOption
import org.apache.poi.hssf.usermodel.{HSSFCell, HSSFRow, HSSFSheet, HSSFWorkbook}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

/**
 * An Excel-Workbench storing or importing data.
 * @param xlsFile The workbench Excel file
 */
class ExcelWorkbench(val xlsFile: Workbenchfile) {

	val log = Logging(system, getClass)

	private lazy val workBook : HSSFWorkbook = xlsFile.inputStream.apply() match {
		case Success(stream) =>
			log.info(s"""Reading from '${xlsFile.resourceName}...""")
			new HSSFWorkbook(stream)
		case Failure(exception) =>
			log.error(s"""Opening '${xlsFile.resourceName}' failed: $exception""")
			log.info(s"""Creating new workbook '${xlsFile.resourceName}'.""")
			new HSSFWorkbook()
	}

	def sheetNames: Seq[String] = {
		for ( i <- 0 until workBook.getNumberOfSheets )
			yield workBook.getSheetAt(i).getSheetName
	}

	private[ExcelWorkbench] def getOrCreateSheet[T](sheetName: Option[String], classTag: ClassTag[T]): HSSFSheet = {
		val name = sheetName.getOrElse(classTag.runtimeClass.getSimpleName)
		Option(workBook.getSheet(name)) match {
			case Some(s) =>
				log.info(s"""(${xlsFile.resourceName}) Reading data from sheet '${s.getSheetName}'.""")
				s
			case None =>
				log.error(s"""(${xlsFile.resourceName}) No sheet has been found named '${name}', create new""")
				workBook.createSheet(name)
		}
	}

	private[ExcelWorkbench] def getOrCreateRow(sheet: HSSFSheet, rowIndex: Int): HSSFRow = {
		Option(sheet.getRow(rowIndex)) match {
			case None => sheet.createRow(rowIndex)
			case Some(r) => r
		}
	}

	private[ExcelWorkbench] def getOrCreateCell(row: HSSFRow, colIndex: Int): HSSFCell = {
		Option(row.getCell(colIndex)) match {
			case None => row.createCell(colIndex)
			case Some(c) => c
		}
	}

	// Temporary
	def fixedFormatReadAttribute(sheetName: Option[String] = None, startRow: Int = 0, endRow: Option[Int] = None): Seq[Attribute] = {
		implicit val classTag = implicitly[ClassTag[Attribute]]
		val sheet = getOrCreateSheet(sheetName, classTag)

		log.info(s"""See ${sheet.getLastRowNum} rows of ${sheet.getPhysicalNumberOfRows} physically available rows.""")
		for (rowIndex <- startRow to endRow.getOrElse(sheet.getLastRowNum)) yield {
			val r = getOrCreateRow(sheet, rowIndex)
			Attribute(
				toBlankOption(getOrCreateCell(r, 0).getStringCellValue), // id
				toBlankOption(getOrCreateCell(r, 1).getStringCellValue), // parent
				Option(getOrCreateCell(r, 2).getStringCellValue), // name
				toBlankOption(getOrCreateCell(r, 3).getStringCellValue), // description
				AttributeTypes.withName(getOrCreateCell(r, 4).getStringCellValue.toLowerCase), // Type
				AttributeControlTypes.withName(getOrCreateCell(r, 5).getStringCellValue.toLowerCase) // Control Type
			)
		}
	}

	// Temporary
	def fixedFormatReadAttributeGroup(sheetName: Option[String] = None, startRow: Int = 0, endRow: Option[Int] = None): Seq[AttributeGroup] = {
		implicit val classTag = implicitly[ClassTag[AttributeGroup]]
		val sheet = getOrCreateSheet(sheetName, classTag)

		log.info(s"""See ${sheet.getLastRowNum} rows of ${sheet.getPhysicalNumberOfRows} physically available rows.""")
		for (rowIndex <- startRow to endRow.getOrElse(sheet.getLastRowNum)) yield {
			val r = getOrCreateRow(sheet, rowIndex)
			AttributeGroup(
				toBlankOption(getOrCreateCell(r, 0).getStringCellValue), // id
				Option(getOrCreateCell(r, 1).getStringCellValue), // name
				Option(getOrCreateCell(r,2).getStringCellValue), // description
			)
		}
	}

	def readDto[T](sheetName: Option[String] = None, startRowIndex: Int = 0)
				  (implicit typeTag:TypeTag[T],  classTag: ClassTag[T]): Unit = {
		val sheet = getOrCreateSheet(sheetName, classTag)
		val headerRow = getOrCreateRow(sheet, startRowIndex)
		var header = for (i <- headerRow.getFirstCellNum to headerRow.getLastCellNum) yield (headerRow.getCell(i).getStringCellValue, i)
		/*
		val columnTags = ColumnTag.create[T](header, true)

		log.info(s"""(${fileName}) Recognized header (${columnTags.map{_.symbol.name.toString}.mkString(",")}).""")

		for (rowIndex <- (startRow + 1) to sheet.getLastRowNum) {
			val rowData = sheet.getRow(rowIndex)

		}
		*/
	}

	def saveDto[T](data : Seq[T], sheetName: Option[String] = None)
				  (implicit typeTag:TypeTag[T],  classTag: ClassTag[T]): Unit = {

		val sheet = getOrCreateSheet[T](sheetName, classTag)

		log.info(s"""(${xlsFile.resourceName}) Saving data to sheet '${sheet.getSheetName}' in progress.""")

		val header = getOrCreateRow(sheet, 0)
		val index = TextMarshaller.caseAccessors[T]

		index.zipWithIndex.foreach { c =>
			header.createCell(c._2).setCellValue(c._1.name.toString)
		}

		data.zipWithIndex.foreach {
			case (row:Any, rowNo:Int) =>
				val sheetRow = getOrCreateRow(sheet, rowNo + 1)
				TextMarshaller.caseValues[T](row.asInstanceOf[T], index).zipWithIndex.foreach { r =>
					getOrCreateCell(sheetRow, r._2).setCellValue(r._1)
				}
		}

		log.info(s"""(${xlsFile.resourceName}) Saving data to sheet '${sheet.getSheetName}' done.""")
	}

	def flush(newFileName: Option[String] = None): Try[Boolean] = {
		if (xlsFile.overwrite)
			Try {
				val output = new FileOutputStream(newFileName.getOrElse(xlsFile.resourceName))
				workBook.write(output)
				output.close()
				true
			}
		else
			Failure[Boolean](new Exception("Overwriting not allowed"))
	}
}
