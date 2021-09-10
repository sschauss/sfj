package org.softlang.sfj.maven

import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import org.eclipse.aether.artifact.{Artifact, ArtifactProperties, DefaultArtifact}
import org.eclipse.aether.collection.CollectResult
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.*
import org.eclipse.aether.resolution.ArtifactResult
import org.slf4j.LoggerFactory
import org.softlang.sfj.core.reduce

import java.io.{File, FileReader, FileWriter}
import java.nio.channels.FileChannel
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*
import scala.util.Try

@main
def resolve(inputCsv: String, outputJson: String): Unit = {
  val logger = LoggerFactory.getLogger(getClass())

  val localRepositoryDirectory = Files.createTempDirectory("repo")
  val fakeLocalRepositoryDirectory = Files.createTempDirectory("fake-repo")

  logger.info(s"Using local repo $localRepositoryDirectory")
  logger.info(s"Using local skinnified repo $fakeLocalRepositoryDirectory")

  given repositorySystem: RepositorySystem = newRepositorySystem()

  given repositorySystemSession: RepositorySystemSession = newRepositorySystemSession(localRepositoryDirectory)

  given remoteRepository: RemoteRepository = newRemoteRepository()

  val localRepositoryManager = repositorySystemSession.getLocalRepositoryManager()
  val fileReader = new FileReader(inputCsv)
  val fileWriter = new FileWriter(Path.of(outputJson).toFile())
  val jsonWriter = new JsonWriter(fileWriter)
  jsonWriter.beginArray()
  csvFormat
    .parse(fileReader)
    .asScala
    .foreach(record => {
      Try {
        val groupId = record.get("groupId")
        val artifactId = record.get("artifactId")
        val version = record.get("version")
        val artiact: Artifact = resolveArtifact(new DefaultArtifact(groupId, artifactId, "pom", version));
        val dependencies: Vector[Artifact] = resolveDependencies(artiact)
        val inputPaths = dependencies
          .map(_.getFile.getAbsolutePath())
          .map(Path.of(_))
        val outputPaths = dependencies
          .map(localRepositoryManager.getPathForLocalArtifact)
          .map(fakeLocalRepositoryDirectory.resolve(_))

        inputPaths
          .zip(outputPaths)
          .foreach(reduce)

        jsonWriter.beginObject()
        jsonWriter.name("groupId").value(groupId)
        jsonWriter.name("artifactId").value(artifactId)
        jsonWriter.name("version").value(version)
        jsonWriter.name("dependencies")
        jsonWriter.beginArray()
        inputPaths
          .zip(outputPaths)
          .zip(dependencies)
          .foreach({
            case ((inputPath, outputPath), artifact) =>
              jsonWriter.beginObject()
              jsonWriter.name("groupId").value(artifact.getArtifactId())
              jsonWriter.name("artifactId").value(artifact.getGroupId())
              jsonWriter.name("version").value(artifact.getVersion())
              jsonWriter.name("originalSize").value(FileChannel.open(inputPath).size())
              jsonWriter.name("skinnifiedSize").value(FileChannel.open(outputPath).size())
              jsonWriter.endObject()
          })
        jsonWriter.endArray()
        jsonWriter.endObject()
      }

    })
  jsonWriter.endArray()
  jsonWriter.close()
  fileWriter.close()
  fileReader.close()

}
