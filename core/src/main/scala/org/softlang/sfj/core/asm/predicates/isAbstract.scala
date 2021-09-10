package org.softlang.sfj.core.asm.predicates

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

def isAbstract(classNode: ClassNode): Boolean = isAbstract(classNode.access)

def isAbstract(innerClassNode: InnerClassNode): Boolean = isAbstract(innerClassNode.access)

def isAbstract(methodNode: MethodNode): Boolean = isAbstract(methodNode.access)

def isAbstract(fieldNode: FieldNode): Boolean = isAbstract(fieldNode.access)

private def isAbstract(access: Int): Boolean = (access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT
