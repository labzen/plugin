package cn.labzen.plugin.broker.impl.specific.reflection

import cn.labzen.cells.core.definition.Constants
import cn.labzen.plugin.api.dev.Configurable
import cn.labzen.plugin.api.dev.Extensible
import cn.labzen.plugin.api.dev.Mountable
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.api.dev.annotation.Configuration
import cn.labzen.plugin.api.dev.annotation.Extension
import cn.labzen.plugin.api.dev.annotation.Mount
import cn.labzen.plugin.api.event.Publishable
import cn.labzen.plugin.api.event.Subscribable
import cn.labzen.plugin.api.event.annotation.Publish
import cn.labzen.plugin.api.event.annotation.Subscribe
import cn.labzen.plugin.broker.exception.PluginInstantiateException
import java.io.File
import java.net.JarURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.jar.JarFile

internal class PluginReflector(pluggableClass: Class<Pluggable>) {

  private val classloader = pluggableClass.classLoader
  private val rootPackage = pluggableClass.`package`.name

  private val scannedClass: Map<Pair<Class<out Annotation>, Class<out Any>>, MutableList<Class<*>>> = mapOf(
    Pair(CONFIGURABLE_CLASSES, mutableListOf()),
    Pair(EXTENSIBLE_CLASSES, mutableListOf()),
    Pair(MOUNTABLE_CLASSES, mutableListOf()),
    Pair(PUBLISHABLE_CLASSES, mutableListOf()),
    Pair(SUBSCRIBABLE_CLASSES, mutableListOf())
  )

  fun get(defined: Pair<Class<out Annotation>, Class<out Any>>): List<Class<*>>? =
    scannedClass[defined]?.toList()

  fun scan() {
    val packagePath = rootPackage.replace('.', '/')
    try {
      val resources = classloader.getResources(packagePath)
      while (resources.hasMoreElements()) {
        scanResource(resources.nextElement())
      }
    } catch (e: Exception) {
      throw PluginInstantiateException(e, "无法获取插件资源 [$packagePath]")
    }
  }

  private fun scanResource(resource: URL) {
    when (resource.protocol) {
      "file" -> {
        val path = URLDecoder.decode(resource.file, Constants.DEFAULT_CHARSET)
        scanFromFiles(path, rootPackage)
      }

      "jar" -> {
        val jar = (resource.openConnection() as JarURLConnection).jarFile
        scanFromJar(jar)
      }
    }
  }

  private fun scanFromFiles(path: String, pkg: String) {
    val file = File(path)
    if (!file.exists()) {
      return
    }

    val subFiles = file.listFiles { f ->
      f.isDirectory || f.name.endsWith(".class")
    }
    subFiles?.forEach {
      val absPath = it.absolutePath
      if (it.isDirectory) {
        scanFromFiles(absPath, "$pkg.${it.name}")
      } else {
        val className = "$pkg.${it.name.removeSuffix(".class")}"
        scanClass(className)
      }
    }
  }

  private fun scanFromJar(jar: JarFile) {
    val entries = jar.entries()
    while (entries.hasMoreElements()) {
      val entry = entries.nextElement()
      val name = entry.name.removePrefix("/").replace('/', '.')

      if (entry.isDirectory || !name.endsWith(".class")) {
        continue
      }
      if (!name.startsWith(rootPackage)) {
        continue
      }

      val className = name.removeSuffix(".class")
      scanClass(className)
    }
  }

  private fun scanClass(className: String) {
    val cls = try {
      Class.forName(className, true, classloader)
    } catch (e: Exception) {
      return
    }

    if (cls.annotations.isEmpty() || cls.interfaces.isEmpty()) {
      return
    }

    scannedClass.keys.forEach {
      val annotationPresent = cls.isAnnotationPresent(it.first)
      val interfacePresent = cls.interfaces.contains(it.second)
      if (annotationPresent && interfacePresent) {
        scannedClass[it]!!.add(cls)
      }
    }
  }

  companion object {
    internal val CONFIGURABLE_CLASSES = Pair(Configuration::class.java, Configurable::class.java)
    internal val EXTENSIBLE_CLASSES = Pair(Extension::class.java, Extensible::class.java)
    internal val MOUNTABLE_CLASSES = Pair(Mount::class.java, Mountable::class.java)
    internal val PUBLISHABLE_CLASSES = Pair(Publish::class.java, Publishable::class.java)
    internal val SUBSCRIBABLE_CLASSES = Pair(Subscribe::class.java, Subscribable::class.java)
  }
}
