package cn.labzen.plugin.api.broker

interface Mount {

  /**
   * 设置挂载组件参数
   */
  fun setArgument(name: String, value: Any)

  /**
   * 完成挂载
   */
  fun done()

  fun extending(extensibleName: String): Extension
}
