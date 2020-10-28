package org.kubicz.mavenexecutor.view.components

import com.intellij.ui.CheckboxTreeListener
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.ThreeStateCheckBox
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeCellRenderer

@Suppress("UNUSED_PARAMETER")
open class CustomCheckboxTreeBase @JvmOverloads constructor(cellRenderer: CheckboxTreeCellRendererBase = CheckboxTreeCellRendererBase(),
                                                            root: CheckedTreeNode? = null,
                                                            checkPolicy: CheckPolicy = CustomCheckboxTreeHelper.DEFAULT_POLICY) : Tree() {

    private val helper: CustomCheckboxTreeHelper

    private val myEventDispatcher = EventDispatcher.create(CheckboxTreeListener::class.java)
    private var expandListener: CheckboxTreeExpandListener? = null

    init {
        helper = CustomCheckboxTreeHelper(checkPolicy, myEventDispatcher)
        if (root != null) {
            model = DefaultTreeModel(root)
            setSelectionRow(0)
        }
        myEventDispatcher.addListener(object : CheckboxTreeListener {
            override fun mouseDoubleClicked(node: CheckedTreeNode) {
                onDoubleClick(node)
            }

            override fun nodeStateChanged(node: CheckedTreeNode) {
                this@CustomCheckboxTreeBase.onNodeStateChanged(node)
            }

            override fun beforeNodeStateChanged(node: CheckedTreeNode) {
                this@CustomCheckboxTreeBase.nodeStateWillChange(node)
            }
        })
        helper.initTree(this, this, cellRenderer)
    }

    fun addCheckboxTreeListener(listener: CheckboxTreeListener) {
        myEventDispatcher.addListener(listener)
    }

    fun setCheckboxTreeExpandListener(listener: CheckboxTreeExpandListener) {
        helper.expandListener = listener
    }

    fun setNodeState(node: CheckedTreeNode, checked: Boolean) {
        helper.setNodeState(this, node, checked)
    }

    protected fun onDoubleClick(node: CheckedTreeNode) {}

    override fun getToggleClickCount(): Int {
        return -1
    }

    protected fun onNodeStateChanged(node: CheckedTreeNode) {}

    protected fun nodeStateWillChange(node: CheckedTreeNode) {}

    open class CheckboxTreeCellRendererBase @JvmOverloads constructor(opaque: Boolean = true, private val myUsePartialStatusForParentNodes: Boolean = true) : JPanel(BorderLayout()), TreeCellRenderer {
        val textRenderer: ColoredTreeCellRenderer
        val myCheckbox = ThreeStateCheckBox()
        private var myIgnoreInheritance: Boolean = false

        init {
            myCheckbox.isSelected = false
            myCheckbox.isThirdStateEnabled = false
            textRenderer = object : ColoredTreeCellRenderer() {
                override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {}
            }
            textRenderer.isOpaque = opaque
            add(myCheckbox, BorderLayout.WEST)
            add(textRenderer, BorderLayout.CENTER)
        }

        override fun getTreeCellRendererComponent(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            invalidate()
            if (value is CheckedTreeNode) {

                val state = getNodeStatus(value)
                myCheckbox.isVisible = true
                myCheckbox.isSelected = state != NodeState.CLEAR
                myCheckbox.isEnabled = value.isEnabled && state != NodeState.PARTIAL
                myCheckbox.isOpaque = false
                myCheckbox.background = null
                background = null

                if (UIUtil.isUnderWin10LookAndFeel()) {
                    val hoverValue = getClientProperty(UIUtil.CHECKBOX_ROLLOVER_PROPERTY)
                    myCheckbox.model.isRollover = hoverValue === value

                    val pressedValue = getClientProperty(UIUtil.CHECKBOX_PRESSED_PROPERTY)
                    myCheckbox.model.isPressed = pressedValue === value
                }
            } else {
                myCheckbox.isVisible = false
            }
            textRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)

//            if (UIUtil.isUnderGTKLookAndFeel()) {
//                val background = if (selected) UIUtil.getTreeSelectionBackground() else UIUtil.getTreeTextBackground()
//                UIUtil.changeBackGround(this, background)
//            }
            customizeRenderer(tree, value, selected, expanded, leaf, row, hasFocus)
            revalidate()

            return this
        }

        private fun getNodeStatus(node: CheckedTreeNode): NodeState {
            if (myIgnoreInheritance) return if (node.isChecked) NodeState.FULL else NodeState.CLEAR
            val checked = node.isChecked
            if (node.childCount == 0 || !myUsePartialStatusForParentNodes) return if (checked) NodeState.FULL else NodeState.CLEAR

            var result: NodeState? = null

            for (i in 0 until node.childCount) {
                val child = node.getChildAt(i)
                val childStatus = if (child is CheckedTreeNode)
                    getNodeStatus(child)
                else if (checked) NodeState.FULL else NodeState.CLEAR
                if (childStatus == NodeState.PARTIAL) return NodeState.PARTIAL
                if (result == null) {
                    result = childStatus
                } else if (result != childStatus) {
                    return NodeState.PARTIAL
                }
            }

            return result ?: NodeState.CLEAR
        }

        open fun customizeRenderer(tree: JTree,
                                   value: Any,
                                   selected: Boolean,
                                   expanded: Boolean,
                                   leaf: Boolean,
                                   row: Int,
                                   hasFocus: Boolean) {
            if (value is CheckedTreeNode) {

            }
        }

    }

    enum class NodeState {
        FULL, CLEAR, PARTIAL
    }

    class CheckPolicy(internal val checkChildrenWithCheckedParent: Boolean,
                      internal val uncheckChildrenWithUncheckedParent: Boolean,
                      internal val checkParentWithCheckedChild: Boolean,
                      internal val uncheckParentWithUncheckedChild: Boolean)
}
