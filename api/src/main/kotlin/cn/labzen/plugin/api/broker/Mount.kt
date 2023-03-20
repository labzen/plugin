package cn.labzen.plugin.api.broker

interface Mount {

  /**
   * 获取创建的挂载实例符号，用于唯一标识一个挂载组件，上层应用（宿主）可使用此标识来持有，
   * 插件也可在发布订阅时，通过该标识传递给上层应用（宿主）来表达事件的归属
   */
  fun identifier(): String

  /**
   * 设置挂载组件参数
   */
  fun setArgument(name: String, value: Any)

  /**
   * 完成挂载
   */
  fun done()

  /**
   * 卸载本挂载组件
   */
  fun unmounting()

  /**
   * 获得一个新的服务扩展实例
   */
  fun extending(extensibleName: String): Extension

  /**
   * 获得一个服务扩展单例，该单例只针对本挂载组件存在，即每一个挂载组件对相同名称的扩展都拥有一个唯一的服务扩展单例
   */
  fun extendingSingleton(extensibleName: String): Extension
}
