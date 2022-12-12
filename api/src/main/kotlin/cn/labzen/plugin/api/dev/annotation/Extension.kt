package cn.labzen.plugin.api.dev.annotation

/**
 * 标识一个实际功能扩展服务接口
 *
 * ```java
 * @Extension(name = "upload", description = "提供OSS文件上传服务")
 * public class OssUploadExtension implements Extensible {
 *
 *   @Mounted
 *   private AliYunOssAccount ossAccount;
 *   @ExtensionParameter(description = "要上传的本地文件地址", require = true)
 *   private String filePath;
 *
 *   public void execute() {
 *     // 执行上传操作
 *   }
 *
 *   public void destructing() {
 *
 *   }
 * }
 * ```
 *
 * @property name 扩展服务名称，用于创建扩展服务时的唯一标识
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Extension(
  val name: String,
  val description: String
)
