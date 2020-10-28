package org.kubicz.mavenexecutor.view.components

import com.intellij.ui.CheckedTreeNode

interface CheckboxTreeExpandListener {
    fun possibleStageChange(node: CheckedTreeNode, isExpand: Boolean)
}