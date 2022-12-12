package cn.labzen.plugin.api.dev.annotation

/**
 * 标识一个挂载组件（部件）接口，一个具体的单位（硬件或账号等），用于精确表示扩展服务的功用对象
 *
 * ```java
 * @Mount(name = "oss_account",
 *        description = "阿里云存储OSS平台账号",
 *        declarations = {@MountDeclaration(name = "厂商", description = "阿里云"),
 *                        @MountDeclaration(name = "通讯协议", description = "HTTP"),
 *                        @MountDeclaration(name = "占用本机端口", description = "无")})
 * public class AliYunOssAccount implements Mountable {
 *
 *   @MountArgument(description = "OSS平台用户名", require = true)
 *   private String username;
 *   @MountArgument(description = "OSS平台用户密码", require = true)
 *   private String password;
 *
 *   public void onMounted() {
 *     // 登录账号登操作
 *   }
 *
 *   public void onUnmount() {
 *     // 退出账号的相关操作
 *   }
 * }
 * ```
 * @property name 挂载组件名称，用于创建组件时的唯一标识
 * @property description 组件描述
 * @property declarations 组件的信息声明
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mount(
  val name: String,
  val description: String,
  val declarations: Array<MountDeclaration> = []
)
