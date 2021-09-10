package org.softlang.sfj.core.asm

import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode

object VoidType {
  def unapply(t: Type): Boolean = t == Type.VOID_TYPE
}

object PrimitiveType {
  def unapply(t: Type): Boolean =
    Set(
      Type.INT_TYPE,
      Type.VOID_TYPE,
      Type.BOOLEAN_TYPE,
      Type.BYTE_TYPE,
      Type.CHAR_TYPE,
      Type.SHORT_TYPE,
      Type.DOUBLE_TYPE,
      Type.FLOAT_TYPE,
      Type.LONG_TYPE
    ) contains t
}

object ReferenceType {
  def unapply(t: Type): Boolean = t match {
    case VoidType() => false
    case PrimitiveType() => false
    case _ => true
  }
}