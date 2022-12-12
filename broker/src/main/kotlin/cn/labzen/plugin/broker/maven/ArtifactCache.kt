package cn.labzen.plugin.broker.maven

internal class ArtifactCache {

  // 在依赖的深度搜索过程中开启MavenDependencyRegistry，防止互相依赖，从而产生无限重复遍历，造成StackOverflowException
  private val cache = mutableMapOf<String, Artifact>()

  fun cache(artifact: Artifact) {
    cache[artifact.toString()] = artifact
  }

  fun cached(artifact: Artifact): Boolean =
    cache.contains(artifact.toString())
}
