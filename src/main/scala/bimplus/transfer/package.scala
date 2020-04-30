package bimplus

import java.nio.file.Path

package object transfer {

	trait Workbench {
		def workbenchPath: Path
	}

	implicit object LocalWorkbench extends Workbench {
		val workbenchPath: Path = Path.of("~/Documents")
	}
}
