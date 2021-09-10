package org.softlang.sfj.core.iterator.extensions


extension[T] (i: Iterator[T]) {

  def andFinally(f: () => Unit): Iterator[T] = {
    new Iterator[T] {
      def next: T = i.next()

      def hasNext: Boolean = {
        val hasNext = i.hasNext
        if (!hasNext) {
          f()
        }
        hasNext
      }
    }
  }

}
