@file:Suppress("DEPRECATION")

package cn.labzen.plugin.broker.loader.jcl

import org.springframework.boot.loader.archive.Archive
import org.springframework.boot.loader.jar.Handler
import org.springframework.boot.loader.jar.JarFile
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.JarURLConnection
import java.net.URL
import java.net.URLClassLoader
import java.net.URLConnection
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import java.util.*
import java.util.function.Supplier
import java.util.jar.Manifest

class PluginClassLoader(name: String, urls: Array<URL>, parent: ClassLoader) : URLClassLoader(name, urls, parent) {

  companion object {
    init {
      ClassLoader.registerAsParallelCapable()
    }

    private const val BUFFER_SIZE = 4096
  }

  private val exploded = false

  private val rootArchive: Archive? = null

  private val packageLock = Any()

  @Volatile
  private var definePackageCallType: DefinePackageCallType? = null

  override fun findResource(name: String?): URL? {
    if (exploded) {
      return super.findResource(name)
    }
    Handler.setUseFastConnectionExceptions(true)
    return try {
      super.findResource(name)
    } finally {
      Handler.setUseFastConnectionExceptions(false)
    }
  }

  @Throws(IOException::class)
  override fun findResources(name: String?): Enumeration<URL> {
    if (exploded) {
      return super.findResources(name)
    }
    Handler.setUseFastConnectionExceptions(true)
    return try {
      UseFastConnectionExceptionsEnumeration(super.findResources(name))
    } finally {
      Handler.setUseFastConnectionExceptions(false)
    }
  }

  @Throws(ClassNotFoundException::class)
  override fun loadClass(name: String, resolve: Boolean): Class<*>? {
    if (name.startsWith("org.springframework.boot.loader.jarmode.")) {
      try {
        val result = loadClassInLaunchedClassLoader(name)
        if (resolve) {
          resolveClass(result)
        }
        return result
      } catch (ex: ClassNotFoundException) {
        // ignore
      }
    }
    if (exploded) {
      return super.loadClass(name, resolve)
    }
    Handler.setUseFastConnectionExceptions(true)
    return try {
      try {
        definePackageIfNecessary(name)
      } catch (ex: IllegalArgumentException) {
        // Tolerate race condition due to being parallel capable
        if (getPackage(name) == null) {
          // This should never happen as the IllegalArgumentException indicates
          // that the package has already been defined and, therefore,
          // getPackage(name) should not return null.
          throw AssertionError("Package $name has already been defined but it could not be found")
        }
      }
      super.loadClass(name, resolve)
    } finally {
      Handler.setUseFastConnectionExceptions(false)
    }
  }

  @Throws(ClassNotFoundException::class)
  private fun loadClassInLaunchedClassLoader(name: String): Class<*> {
    val internalName = name.replace('.', '/') + ".class"
    val inputStream = parent.getResourceAsStream(internalName) ?: throw ClassNotFoundException(name)
    return try {
      inputStream.use { `is` ->
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        var bytesRead: Int
        while (`is`.read(buffer).also { bytesRead = it } != -1) {
          outputStream.write(buffer, 0, bytesRead)
        }
        val bytes = outputStream.toByteArray()
        val definedClass = defineClass(name, bytes, 0, bytes.size)
        definePackageIfNecessary(name)
        definedClass
      }
    } catch (ex: IOException) {
      throw ClassNotFoundException("Cannot load resource for class [$name]", ex)
    }
  }

  /**
   * Define a package before a `findClass` call is made. This is necessary to
   * ensure that the appropriate manifest for nested JARs is associated with the
   * package.
   * @param className the class name being found
   */
  private fun definePackageIfNecessary(className: String) {
    val lastDot = className.lastIndexOf('.')
    if (lastDot >= 0) {
      val packageName = className.substring(0, lastDot)
      if (getPackage(packageName) == null) {
        try {
          definePackage(className, packageName)
        } catch (ex: IllegalArgumentException) {
          // Tolerate race condition due to being parallel capable
          if (getPackage(packageName) == null) {
            // This should never happen as the IllegalArgumentException
            // indicates that the package has already been defined and,
            // therefore, getPackage(name) should not have returned null.
            throw AssertionError(
              "Package $packageName has already been defined but it could not be found"
            )
          }
        }
      }
    }
  }

  private fun definePackage(className: String, packageName: String) {
    try {
      AccessController.doPrivileged(PrivilegedExceptionAction<Any?> {
        val packageEntryName = packageName.replace('.', '/') + "/"
        val classEntryName = className.replace('.', '/') + ".class"
        for (url in urLs) {
          try {
            val connection = url.openConnection()
            if (connection is JarURLConnection) {
              val jarFile = connection.jarFile
              if (jarFile.getEntry(classEntryName) != null &&
                jarFile.getEntry(packageEntryName) != null &&
                jarFile.manifest != null
              ) {
                definePackage(packageName, jarFile.manifest, url)
                return@PrivilegedExceptionAction null
              }
            }
          } catch (ex: IOException) {
            // Ignore
          }
        }
        null
      }, AccessController.getContext())
    } catch (ex: PrivilegedActionException) {
      // Ignore
    }
  }

  @Throws(IllegalArgumentException::class)
  override fun definePackage(name: String?, man: Manifest?, url: URL?): Package? {
    if (!exploded) {
      return super.definePackage(name, man, url)
    }
    synchronized(packageLock) {
      return doDefinePackage(
        DefinePackageCallType.MANIFEST
      ) { super.definePackage(name, man, url) }
    }
  }

  @Throws(IllegalArgumentException::class)
  override fun definePackage(
    name: String?, specTitle: String?, specVersion: String?, specVendor: String?,
    implTitle: String?, implVersion: String?, implVendor: String?, sealBase: URL?
  ): Package? {
    if (!exploded) {
      return super.definePackage(
        name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor,
        sealBase
      )
    }
    synchronized(packageLock) {
      if (definePackageCallType == null) {
        // We're not part of a call chain which means that the URLClassLoader
        // is trying to define a package for our exploded JAR. We use the
        // manifest version to ensure package attributes are set
        val manifest = getManifest(rootArchive)
        if (manifest != null) {
          return definePackage(name, manifest, sealBase)
        }
      }
      return doDefinePackage(
        DefinePackageCallType.ATTRIBUTES
      ) {
        super.definePackage(
          name, specTitle,
          specVersion, specVendor, implTitle, implVersion, implVendor, sealBase
        )
      }
    }
  }

  private fun getManifest(archive: Archive?): Manifest? {
    return try {
      archive?.manifest
    } catch (ex: IOException) {
      null
    }
  }

  private fun <T> doDefinePackage(type: DefinePackageCallType, call: Supplier<T>): T {
    val existingType = definePackageCallType
    return try {
      definePackageCallType = type
      call.get()
    } finally {
      definePackageCallType = existingType
    }
  }

  /**
   * Clear URL caches.
   */
  fun clearCache() {
    if (exploded) {
      return
    }
    for (url in urLs) {
      try {
        val connection = url.openConnection()
        (connection as? JarURLConnection)?.let { clearCache(it) }
      } catch (ex: IOException) {
        // Ignore
      }
    }
  }

  @Throws(IOException::class)
  private fun clearCache(connection: URLConnection) {
    val jarFile: Any = (connection as JarURLConnection).jarFile
    if (jarFile is JarFile) {
      jarFile.clearCache()
    }
  }

  private class UseFastConnectionExceptionsEnumeration(private val delegate: Enumeration<URL>) :
    Enumeration<URL> {

    override fun hasMoreElements(): Boolean {
      Handler.setUseFastConnectionExceptions(true)
      return try {
        delegate.hasMoreElements()
      } finally {
        Handler.setUseFastConnectionExceptions(false)
      }
    }

    override fun nextElement(): URL {
      Handler.setUseFastConnectionExceptions(true)
      return try {
        delegate.nextElement()
      } finally {
        Handler.setUseFastConnectionExceptions(false)
      }
    }
  }

  /**
   * The different types of call made to define a package. We track these for exploded
   * jars so that we can detect packages that should have manifest attributes applied.
   */
  private enum class DefinePackageCallType {
    /**
     * A define package call from a resource that has a manifest.
     */
    MANIFEST,

    /**
     * A define package call with a direct set of attributes.
     */
    ATTRIBUTES
  }
}
