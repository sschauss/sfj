package org.softlang.sfj.core.asm.predicates

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

def isStatic(methodNode: MethodNode): Boolean = isStatic(methodNode.access)

def isStatic(classNode: ClassNode): Boolean = isStatic(classNode.access)

def isStatic(fieldNode: FieldNode): Boolean = isStatic(fieldNode.access)

private def isStatic(access: Int): Boolean = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
