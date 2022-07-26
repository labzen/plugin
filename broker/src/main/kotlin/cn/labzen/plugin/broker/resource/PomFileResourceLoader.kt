package cn.labzen.plugin.broker.resource

import java.io.File

open class PomFileResourceLoader(file: File) : FileResourceLoader(file) {

  // constructor(location: String) : this(File(location))
  // constructor(path: Path) : this(path.toFile())

  override fun strictFilenamePattern(): String = STRICT_FILENAME_PATTERN

  companion object {
    private const val STRICT_FILENAME_PATTERN = "(.*\\.pom)|(pom\\.xml)"
  }
}
