package org.softlang.sfj.core

import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.softlang.sfj.core.asm.*
import org.softlang.sfj.core.asm.predicates.*

import java.net.URI
import java.nio.file.*
import java.util.UUID
import java.util.jar.*
import scala.annotation.targetName
import scala.concurrent.*
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.*

def reduce(inputPath: Path, outputPath: Path): Path = {
  Files.createDirectories(outputPath.getParent())
  val classNodes = inputPath
    .classNodes()
    .map(reduceClassNode)
    .toVector
  classNodes
    .iterator
    .toPath(outputPath)
}

private def reduceClassNode(classNode: ClassNode): ClassNode = {
  given executionContext: ExecutionContext = global
  classNode.methods = classNode.methods
    .asScala
    .filterNot(isPrivate)
    .map(reduceMethodNode)
    .asJava
  classNode.fields = classNode.fields
    .asScala
    .filterNot(isPrivate)
    .map(reduceFieldNode)
    .asJava
  classNode
}

private def reduceMethodNode(methodNode: MethodNode): MethodNode = {
  if (isAbstract(methodNode)) {
    return methodNode
  }
  val instructions = new InsnList()
  methodNode.name match {
    case "<init>" =>
      instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
      Type
        .getArgumentTypes(methodNode.superInitMethodInsn.desc)
        .foreach({
          case PrimitiveType() =>
            instructions.add(new InsnNode(Opcodes.ICONST_0))
          case ReferenceType() =>
            instructions.add(new InsnNode(Opcodes.ACONST_NULL))
        })
      instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, methodNode.superInitMethodInsn.owner, "<init>", methodNode.superInitMethodInsn.desc, methodNode.superInitMethodInsn.itf))
      instructions.add(new InsnNode(Opcodes.RETURN))
    case _ => Type.getReturnType(methodNode.desc) match {
      case VoidType() =>
        instructions.add(new InsnNode(Opcodes.RETURN))
      case PrimitiveType() =>
        instructions.add(new InsnNode(Opcodes.ICONST_0))
        instructions.add(new InsnNode(Opcodes.IRETURN))
      case ReferenceType() =>
        instructions.add(new InsnNode(Opcodes.ACONST_NULL))
        instructions.add(new InsnNode(Opcodes.ARETURN))
    }
  }
  methodNode.instructions = instructions
  methodNode.tryCatchBlocks = List().asJava
  methodNode
}

private def reduceFieldNode(fieldNode: FieldNode): FieldNode = {
  if (isAbstract(fieldNode)) {
    return fieldNode
  }
  Type.getType(fieldNode.desc) match {
    case PrimitiveType() =>
      fieldNode.value = 0
    case ReferenceType() =>
      fieldNode.value = null
  }
  fieldNode
}