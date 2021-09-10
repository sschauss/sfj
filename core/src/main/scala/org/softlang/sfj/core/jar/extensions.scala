package org.softlang.sfj.core.jar.extensions

import java.util.jar.*
import scala.jdk.CollectionConverters.*

extension (jarInputStream: JarInputStream) {

  def classEntries(): Iterator[JarEntry] =
    Iterator
      .continually(jarInputStream.getNextJarEntry)
      .takeWhile(_ != null)
      .filter(_.getName.endsWith(".class"))

}