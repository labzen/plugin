package cn.labzen.plugin.api.broker

interface Mount {

  /**
   * 获取创建的挂载实例符号，用于唯一标识一个挂载物，上层应用（宿主）可使用此标识来持有，
   * 插件也可在发布订阅时，通过该标识传递给上层应用（宿主）来表达事件的归属
   */
  fun getSymbol(): String

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
