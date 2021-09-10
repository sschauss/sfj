package org.softlang.sfj.maven

import java.nio.file.{Files, Path}
import java.util.Comparator

def deletePath(path: Path): Unit = {
  Files
    .walk(path)
    .sorted(Comparator.reverseOrder())
    .forEach(Files.deleteIfExists)
}