package org.kubicz.mavenexecutor.view.components

import com.intellij.openapi.util.Key
import com.intellij.ui.CheckboxTreeListener
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.ClickListener
import com.intellij.ui.speedSearch.SpeedSearchSupply
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

class CustomCheckboxTreeHelper(private val checkPolicy: CustomCheckboxTreeBase.CheckPolicy, private val myEventDispatcher: EventDispatcher<CheckboxTreeListener>) {

    fun initTree(tree: Tree, mainComponent: JComponent, cellRenderer: CustomCheckboxTreeBase.CheckboxTreeCellRendererBase) {
        removeTreeListeners(mainComponent)
        tree.cellRenderer = cellRenderer
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.setLineStyleAngled()
        TreeUtil.installActions(tree)

        val keyListener = setupKeyListener(tree, mainComponent)
        val clickListener = setupMouseListener(tree, mainComponent, cellRenderer)
        UIUtil.putClientProperty(mainComponent, TREE_LISTENERS_REMOVER, Runnable {
            mainComponent.removeKeyListener(keyListener)
            clickListener.uninstall(mainComponent)
        })
    }

    fun setNodeState(tree: Tree, node: CheckedTreeNode, checked: Boolean) {
        changeNodeState(node, checked)
        adjustParentsAndChildren(node, checked)
        tree.repaint()

        // notify model listeners about model change
        val model = tree.model
        model.valueForPathChanged(TreePath(node.path), node.userObject)
    }

    private fun toggleNode(tree: Tree, node: CheckedTreeNode) {
        setNodeState(tree, node, !node.isChecked)
    }

    private fun adjustParentsAndChildren(node: CheckedTreeNode, checked: Boolean) {
        if (!checked) {
            if (checkPolicy.uncheckParentWithUncheckedChild) {
                var parent: TreeNode? = node.parent
                while (parent != null) {
                    if (parent is CheckedTreeNode) {
                        changeNodeState((parent as CheckedTreeNode?)!!, false)
                    }
                    parent = parent.parent
                }
            }
            if (checkPolicy.uncheckChildrenWithUncheckedParent) {
                uncheckChildren(node)
            }
        } else {
            if (checkPolicy.checkChildrenWithCheckedParent) {
                checkChildren(node)
            }

            if (checkPolicy.checkParentWithCheckedChild) {
                var parent: TreeNode? = node.parent
                while (parent != null) {
                    if (parent is CheckedTreeNode) {
                        changeNodeState((parent as CheckedTreeNode?)!!, true)
                    }
                    parent = parent.parent
                }
            }
            var parent: TreeNode? = node.parent
            while (parent != null) {
                if (parent is CheckedTreeNode) {
                    if (isChecked((parent as CheckedTreeNode?)!!)) {
                        changeNodeState((parent as CheckedTreeNode?)!!, true)
                    }
                    //     ((CheckedTreeNode) parent).setChecked(isChecked((CheckedTreeNode) parent));
                }
                parent = parent.parent
            }


        }
    }

    private fun isChecked(node: CheckedTreeNode): Boolean {
        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i)
            val childStatus = (child as CheckedTreeNode).isChecked

            if (!childStatus) {
                return false
            }
        }

        return true
    }

    private fun changeNodeState(node: CheckedTreeNode, checked: Boolean) {
        if (node.isChecked != checked) {
            myEventDispatcher.multicaster.beforeNodeStateChanged(node)
            node.isChecked = checked
            myEventDispatcher.multicaster.nodeStateChanged(node)
        }
    }

    private fun uncheckChildren(node: CheckedTreeNode) {
        val children = node.children()
        while (children.hasMoreElements()) {
            val o = children.nextElement() as? CheckedTreeNode ?: continue
            changeNodeState(o, false)
            uncheckChildren(o)
        }
    }

    private fun checkChildren(node: CheckedTreeNode) {
        val children = node.children()
        while (children.hasMoreElements()) {
            val o = children.nextElement() as? CheckedTreeNode ?: continue
            changeNodeState(o, true)
            checkChildren(o)
        }
    }

    private fun setupKeyListener(tree: Tree, mainComponent: JComponent): KeyListener {
        val listener = object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (isToggleEvent(e, mainComponent)) {
                    val treePath = tree.leadSelectionPath ?: return
                    val o = treePath.lastPathComponent as? CheckedTreeNode ?: return
                    if (!o.isEnabled) return
                    toggleNode(tree, o)
                    val checked = o.isChecked

                    val selectionPaths = tree.selectionPaths
                    var i = 0
                    while (selectionPaths != null && i < selectionPaths.size) {
                        val selectionPath = selectionPaths[i]
                        val o1 = selectionPath.lastPathComponent
                        if (o1 !is CheckedTreeNode) {
                            i++
                            continue
                        }
                        setNodeState(tree, o1, checked)
                        i++
                    }

                    e.consume()
                }
            }
        }
        mainComponent.addKeyListener(listener)
        return listener
    }

    private fun setupMouseListener(tree: Tree, mainComponent: JComponent, cellRenderer: CustomCheckboxTreeBase.CheckboxTreeCellRendererBase): ClickListener {
        val listener = object : ClickListener() {
            override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
                val row = tree.getRowForLocation(e.x, e.y)
                if (row < 0) return false
                val o = tree.getPathForRow(row).lastPathComponent as? CheckedTreeNode ?: return false
                val rowBounds = tree.getRowBounds(row)
                cellRenderer.bounds = rowBounds
                val checkBounds = cellRenderer.myCheckbox.bounds
                checkBounds.location = rowBounds.location

                if (checkBounds.height == 0) {
                    checkBounds.width = rowBounds.height
                    checkBounds.height = checkBounds.width
                }

                if (checkBounds.contains(e.point)) {
                    if (o.isEnabled) {
                        toggleNode(tree, o)
                        tree.setSelectionRow(row)
                        return true
                    }
                } else if (clickCount > 1 && clickCount % 2 == 0) {
                    myEventDispatcher.multicaster.mouseDoubleClicked(o)
                    return true
                }

                return false
            }
        }
        listener.installOn(mainComponent)
        return listener
    }

    companion object {

        private val TREE_LISTENERS_REMOVER = Key.create<Runnable>("TREE_LISTENERS_REMOVER")
        val DEFAULT_POLICY = CustomCheckboxTreeBase.CheckPolicy(true, true, false, true)

        fun isToggleEvent(e: KeyEvent, mainComponent: JComponent): Boolean {
            return e.keyCode == KeyEvent.VK_SPACE && SpeedSearchSupply.getSupply(mainComponent) == null
        }

        private fun removeTreeListeners(mainComponent: JComponent) {
            val remover = UIUtil.getClientProperty(mainComponent, TREE_LISTENERS_REMOVER)
            remover?.run()
        }

    }
}
