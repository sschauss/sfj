package org.softlang.sfj.maven

import org.apache.maven.shared.invoker.*
import org.eclipse.aether.artifact.Artifact

import java.io.InputStream
import java.nio.file.Path
import java.util
import java.util.Properties


def invoke(pom: Path, localRepostitory: Path): InvocationResult = {
  val properties = new Properties()
  properties.setProperty("maven.test.skip", "true")
  properties.setProperty("maven.compiler.source", "8")
  properties.setProperty("maven.compiler.target", "8")
  properties.setProperty("maven.compiler.fork", "true")

  val request = new DefaultInvocationRequest()
  request.setInputStream(InputStream.nullInputStream())
  request.setPomFile(pom.toFile())
  request.setGoals(util.Arrays.asList("package"))
  request.setProperties(properties)

  val invoker = new DefaultInvoker()
  invoker.setLocalRepositoryDirectory(localRepostitory.toFile())
  invoker.setWorkingDirectory(pom.getParent().toFile())
  invoker.execute(request)
}