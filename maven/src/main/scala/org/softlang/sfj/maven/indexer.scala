package org.softlang.sfj.maven

import org.apache.commons.csv.{CSVFormat, CSVPrinter}
import org.apache.lucene.index.IndexReader
import org.apache.maven.index.*
import org.apache.maven.index.context.*
import org.apache.maven.index.updater.*
import org.apache.maven.wagon.Wagon
import org.apache.maven.wagon.events.*
import org.codehaus.plexus.*

import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.util.UUID
import scala.jdk.CollectionConverters.*
import scala.util.Random

val csvFormat = CSVFormat.DEFAULT
  .withHeader("groupId", "artifactId", "version", "sourcesExists", "lastModified")
  .withFirstRecordAsHeader()

@main
def index(indexDiretoryPath: String, indexFilePath: String): Unit = {
  val containerConfiguration = new DefaultContainerConfiguration()
  containerConfiguration.setClassPathScanning(PlexusConstants.SCANNING_INDEX)
  val plexusContainer = new DefaultPlexusContainer(containerConfiguration)
  val indexer = plexusContainer.lookup(classOf[Indexer])
  val indexers = List(plexusContainer.lookup(classOf[IndexCreator], "min")).asJava
  val indexDirectory = Files.createDirectories(Path.of(indexDiretoryPath)).toFile
  val indexingContext = indexer.createIndexingContext(
    "central-index",
    "central",
    null,
    indexDirectory,
    "https://repo.maven.apache.org/maven2/",
    null,
    true,
    true,
    indexers
  )

  val indexUpdater = plexusContainer.lookup(classOf[IndexUpdater])
  val httpWagon = plexusContainer.lookup(classOf[Wagon])
  val resourceFetcher = new WagonHelper.WagonFetcher(httpWagon, null, null, null)
  val indexUpdateRequest = new IndexUpdateRequest(indexingContext, resourceFetcher)
  indexUpdateRequest.setIncrementalOnly(true)
  indexUpdateRequest.setForceFullUpdate(false)

  indexUpdater.fetchAndUpdateIndex(indexUpdateRequest)

  val indexSearcher = indexingContext.acquireIndexSearcher()
  val indexReader = indexSearcher.getIndexReader()

  Files.deleteIfExists(Path.of(indexFilePath))
  val fileWriter = new FileWriter(indexFilePath)
  val csvPrinter = new CSVPrinter(fileWriter, csvFormat)
  Iterator
    .range(0, indexReader.maxDoc())
    .map(indexReader.document)
    .map(IndexUtils.constructArtifactInfo(_, indexingContext))
    .filter(_ != null)
    .filter(_.getClassifier() == null)
    .filter(_.getFileExtension() == "jar")
    .filter(_.getPackaging() == "jar")
    .foreach(artifactInfo => {
      val groupId = artifactInfo.getGroupId()
      val artifactId = artifactInfo.getArtifactId()
      val version = artifactInfo.getVersion()
      val sourcesExists = artifactInfo.getSourcesExists()
      val lastModified = artifactInfo.getLastModified()
      csvPrinter.printRecord(groupId, artifactId, version, sourcesExists, lastModified)
    })
  csvPrinter.close(true)
  fileWriter.close()
}