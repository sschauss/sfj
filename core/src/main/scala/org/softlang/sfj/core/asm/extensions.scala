package org.softlang.sfj.core.asm

import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.softlang.sfj.core.asm.predicates.isAbstract
import org.softlang.sfj.core.iterator.extensions.andFinally
import org.softlang.sfj.core.jar.extensions.classEntries

import java.io.*
import java.net.URI
import java.nio.file.*
import java.nio.file.spi.FileSystemProvider
import java.util.jar.*
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*

extension (path: Path) {
  def classNodes(): Iterator[ClassNode] = {
    val env = Map("create" -> "true")
    val uri = URI.create(s"jar:file:$path")
    val fileSystem = FileSystems.newFileSystem(uri, env.asJava)
    val matcher = fileSystem.getPathMatcher("glob:**.class")
    fileSystem
      .getRootDirectories
      .asScala
      .map(Files.find(_, Int.MaxValue, (path, _) => matcher.matches(path)).toScala(Iterator))
      .flatten
      .iterator
      .map(path => {
        val inputStream = Files.newInputStream(path, StandardOpenOption.READ)
        val classReader = new ClassReader(inputStream)
        val classNode = new ClassNode()
        classReader.accept(classNode, 0)
        classNode
      })
      .andFinally(fileSystem.close)
  }
}

extension (classNode: ClassNode) {
  def toByteArray(): Array[Byte] = {
    val classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
    classNode.accept(classWriter)
    classWriter.toByteArray
  }
}

extension (classNodes: Iterator[ClassNode]) {
  def toPath(path: Path): Path = {
    val env = Map("create" -> "true")
    val uri = URI.create(s"jar:file:$path")
    val fileSystem = FileSystems.newFileSystem(uri, env.asJava)
    classNodes
      .andFinally(fileSystem.close)
      .foreach(classNode => {
        val path = fileSystem.getPath(s"${classNode.name}.class")
        if (path.getParent != null) {
          Files.createDirectories(path.getParent)
        }
        Files.write(path, classNode.toByteArray())
      })
    path
  }
}

extension (methodNode: MethodNode) {
  def superInitMethodInsn: MethodInsnNode = {
    methodNode
      .instructions
      .asScala
      .filter(_.getType == AbstractInsnNode.METHOD_INSN)
      .map(_.asInstanceOf[MethodInsnNode])
      .filter(_.name == "<init>")
      .head
  }
}