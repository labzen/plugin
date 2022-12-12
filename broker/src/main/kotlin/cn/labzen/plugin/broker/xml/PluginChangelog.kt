package cn.labzen.plugin.broker.xml

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class PluginChangelog {

  lateinit var version: String

  @JacksonXmlProperty(localName = "content")
  @JacksonXmlElementWrapper(useWrapping = false)
  lateinit var contents: List<String>
}
