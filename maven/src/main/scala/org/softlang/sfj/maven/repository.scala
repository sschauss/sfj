package org.softlang.sfj.maven

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.*
import org.eclipse.aether.artifact.*
import org.eclipse.aether.collection.*
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.*
import org.eclipse.aether.repository.*
import org.eclipse.aether.resolution.{ArtifactRequest, ArtifactResult, DependencyRequest}
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.slf4j.LoggerFactory

import java.net.URI
import java.nio.file.{FileSystems, Files, Path, Paths, StandardCopyOption}
import java.util
import java.util.Comparator
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*

def newRepositoryListener() = {
  new AbstractRepositoryListener {
    private val logger = LoggerFactory.getLogger(this.getClass)

    override def artifactDownloading(event: RepositoryEvent): Unit = {
      logger.info(s"${event.getType} ${event.getArtifact}")
    }

    override def artifactDownloaded(event: RepositoryEvent): Unit = {
      logger.info(s"${event.getType} ${event.getArtifact}")
    }

    override def artifactResolving(event: RepositoryEvent): Unit = {
      logger.info(s"${event.getType} ${event.getArtifact}")
    }

    override def artifactResolved(event: RepositoryEvent): Unit = {
      logger.info(s"${event.getType} ${event.getArtifact}")
    }
  }
}

def newRepositorySystem(): RepositorySystem = {
  val serviceLocator = MavenRepositorySystemUtils.newServiceLocator()
  serviceLocator.addService(classOf[RepositoryConnectorFactory], classOf[BasicRepositoryConnectorFactory])
  serviceLocator.addService(classOf[TransporterFactory], classOf[HttpTransporterFactory])
  serviceLocator.addService(classOf[TransporterFactory], classOf[FileTransporterFactory])
  serviceLocator.getService(classOf[RepositorySystem])
}

def newRepositorySystemSession(localRepositoryDirectory: Path)
                              (using repositorySystem: RepositorySystem): RepositorySystemSession = {
  val repositorySystemSession = MavenRepositorySystemUtils.newSession()
  val localRepository = new LocalRepository(localRepositoryDirectory.toFile)
  val localRepositoryManager = repositorySystem.newLocalRepositoryManager(repositorySystemSession, localRepository)
  repositorySystemSession.setLocalRepositoryManager(localRepositoryManager)
  repositorySystemSession.setRepositoryListener(newRepositoryListener())
  repositorySystemSession
}

def resolveDependencies(artifact: Artifact)
                       (using repositorySystem: RepositorySystem)
                       (using repositorySystemSession: RepositorySystemSession)
                       (using remoteRepository: RemoteRepository): Vector[Artifact] = {
  val dependency = new Dependency(artifact, JavaScopes.COMPILE)
  val collectRequest = new CollectRequest(dependency, util.Arrays.asList(remoteRepository))
  val classPathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE)
  val dependencyRequest = new DependencyRequest(collectRequest, classPathFilter)

  repositorySystem
    .resolveDependencies(repositorySystemSession, dependencyRequest)
    .getArtifactResults()
    .asScala
    .map(_.getArtifact)
    .toVector
    .drop(1)
}

def resolveArtifact(artifact: Artifact)
                   (using repositorySystem: RepositorySystem)
                   (using repositorySystemSession: RepositorySystemSession)
                   (using remoteRepository: RemoteRepository): Artifact = {
  val artifactRequest = new ArtifactRequest()
  artifactRequest.setArtifact(artifact)
  artifactRequest.setRepositories(util.Arrays.asList(remoteRepository))
  repositorySystem
    .resolveArtifact(repositorySystemSession, artifactRequest)
    .getArtifact
}

def newRemoteRepository() = {
  new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build()
}

def unpackArtifact(artifact: Artifact, outputPath: Path): Option[Path] = {
  Files.createDirectories(outputPath)
  deletePath(outputPath)
  artifact.getExtension match {
    case "jar" =>
      val env = Map("create" -> "true")
      val uri = URI.create(s"jar:${artifact.getFile.toURI}")
      val fileSystem = FileSystems.newFileSystem(uri, env.asJava)
      val matcher = fileSystem.getPathMatcher("glob:**")
      fileSystem
        .getRootDirectories
        .asScala
        .map(Files.find(_, Int.MaxValue, (path, _) => matcher.matches(path)).toScala(Iterator))
        .flatten
        .iterator
        .foreach(path => Files.copy(path, Path.of(outputPath.toString(), path.toString()), StandardCopyOption.REPLACE_EXISTING))
      Some(outputPath)
    case _ => None
  }
}