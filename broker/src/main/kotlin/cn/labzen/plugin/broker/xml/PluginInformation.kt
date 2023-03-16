package cn.labzen.plugin.broker.xml

import cn.labzen.plugin.api.broker.Information
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "plugin")
class PluginInformation : Information {

  lateinit var name: String
  lateinit var based: String
  lateinit var pluggable: String

  // todo 从maven包里读取版本号
  lateinit var version: String
  lateinit var description: String

  var tags: List<String>? = null

  @JacksonXmlProperty(localName = "author")
  @JacksonXmlElementWrapper(localName = "authors")
  var authors: List<PluginAuthor>? = null

  @JacksonXmlProperty(localName = "log")
  @JacksonXmlElementWrapper(localName = "changelog")
  lateinit var changelogs: List<PluginChangelog>

  override fun name(): String = name

  override fun version(): String = version ?: ""

  override fun description(): String = description

  override fun tags(): List<String> = tags ?: emptyList()

  override fun authors(): List<Pair<String, String>> =
    authors?.map {
      Pair(it.name, it.email ?: "")
    } ?: emptyList()

  override fun changelogs(): List<Pair<String, List<String>>> =
    changelogs.map {
      Pair(it.version, it.contents)
    }
}
