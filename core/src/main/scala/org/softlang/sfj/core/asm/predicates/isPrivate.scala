package org.softlang.sfj.core.asm.predicates

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

def isPrivate(classNode: ClassNode): Boolean = isPrivate(classNode.access)

def isPrivate(innerClassNode: InnerClassNode): Boolean = isPrivate(innerClassNode.access)

def isPrivate(methodNode: MethodNode): Boolean = isPrivate(methodNode.access)

def isPrivate(fieldNode: FieldNode): Boolean = isPrivate(fieldNode.access)

private def isPrivate(access: Int): Boolean = (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE
