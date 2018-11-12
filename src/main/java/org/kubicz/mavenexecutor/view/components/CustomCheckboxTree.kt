package org.kubicz.mavenexecutor.view.components

import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.TreeSpeedSearch

class CustomCheckboxTree(cellRenderer: CheckboxTreeCellRendererBase, root: CheckedTreeNode?) : CustomCheckboxTreeBase(cellRenderer, root) {

    init {

        installSpeedSearch()
    }

    private fun installSpeedSearch() {
        TreeSpeedSearch(this)
    }
}
