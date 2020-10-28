package org.kubicz.mavenexecutor.view.components

import com.intellij.icons.AllIcons
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import org.kubicz.mavenexecutor.view.MavenExecutorBundle
import org.kubicz.mavenexecutor.view.window.panels.Popup
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.EmptyBorder

class CheckboxCombo(project: Project, selectionChangedListener: SelectionChangedListener): JPanel() {

    private val project = project;

    private val chosenItemsWrappedSubPanel = InputPanel(backgroundColorName())

    private var checkBoxListPopup: Popup? = null

    private val checkBoxList = CustomCheckBoxList()

    override fun updateUI() {
        super.updateUI()

        border = null
    }

    override fun setBorder(border: Border?) {
        super.setBorder(JBUI.Borders.customLine(UIManager.getColor("Component.borderColor"), 1))
    }

    override fun setBackground(bg: Color?) {
        super.setBackground(UIManager.getColor("TextField.background"))
    }

    init {
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                this@CheckboxCombo.repaint()
                this@CheckboxCombo.updateUI()
            }
        })
        border = null

        val componentLayout = GroupLayout(this)
        this.layout = componentLayout

        checkBoxList.setCheckBoxListListener { _, _ ->
            refreshChosenItems()

            selectionChangedListener.selectionChanged(selectedItems)
        }

        val checkBoxListScrollPane = ScrollPaneFactory.createScrollPane(checkBoxList)
        checkBoxListScrollPane.maximumSize = Dimension(1000, 1000)
        checkBoxListScrollPane.minimumSize = Dimension(200, 200)

        chosenItemsWrappedSubPanel.layout = ProfilesWrappedFlowLayout(6, 2)
        chosenItemsWrappedSubPanel.componentOrientation = ComponentOrientation.LEFT_TO_RIGHT;
        chosenItemsWrappedSubPanel.minimumSize = Dimension(0, chosenItemsWrappedSubPanel.size.height)
        chosenItemsWrappedSubPanel.maximumSize = Dimension(Integer.MAX_VALUE, chosenItemsWrappedSubPanel.size.height)

        chosenItemsWrappedSubPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    showCheckBoxListPopup()
                }
            }
        })
        chosenItemsWrappedSubPanel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        val arrowButtonPanel = InputBorderLayoutPanel(backgroundColorName())
        arrowButtonPanel.maximumSize = Dimension(28, arrowButtonPanel.height)
        arrowButtonPanel.size = Dimension(28, arrowButtonPanel.height)
        arrowButtonPanel.preferredSize = Dimension(28, arrowButtonPanel.height)
        arrowButtonPanel.minimumSize = Dimension(28, arrowButtonPanel.height)
        arrowButtonPanel.background = UIManager.getColor(backgroundColorName())

        val arrowButtonLabel = ArrowLabel()

        arrowButtonPanel.add(arrowButtonLabel, BorderLayout.CENTER)
        arrowButtonPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (checkBoxListPopup != null && arrowButtonPanel.isShowing) {
                        checkBoxListPopup!!.cancel()
                        checkBoxListPopup = null
                    }
                    else {
                        showCheckBoxListPopup()
                    }
                }
            }
        })
        arrowButtonPanel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        refreshChosenItems()

        val chosenItemsParentSubPanel = ParentPanel(backgroundColorName(), GridLayout(1, 1))
        chosenItemsParentSubPanel.border = EmptyBorder(2, 0, 5, 0)
        chosenItemsParentSubPanel.background = UIManager.getColor(backgroundColorName())
        chosenItemsParentSubPanel.add(chosenItemsWrappedSubPanel)

        componentLayout.setHorizontalGroup(
                componentLayout.createSequentialGroup()
                        .addComponent(chosenItemsParentSubPanel)
                        .addComponent(arrowButtonPanel)
        )
        componentLayout.setVerticalGroup(
                componentLayout.createSequentialGroup()
                        .addGroup(componentLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(chosenItemsParentSubPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                                .addComponent(arrowButtonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                        )
        )


    }

    class ArrowLabel: JLabel() {

        override fun updateUI() {
            super.updateUI()

            if (LafManager.getInstance().currentLookAndFeel?.className == "com.intellij.laf.win10.WinIntelliJLaf") {
                icon = IconLoader.getIcon("/icons/comboDropTriangle.svg", this.javaClass)
                border = JBUI.Borders.empty(7)
            }
            else {
                icon = AllIcons.General.ArrowDown
                border = JBUI.Borders.empty(5)
            }
        }
    }

    class InputBorderLayoutPanel(private val backgroundColorName: String): BorderLayoutPanel() {
        override fun setBackground(bg: Color?) {
            if (this.backgroundColorName != null) {
                super.setBackground(UIManager.getColor(backgroundColorName))
            }
        }

    }

    class ParentPanel : JPanel {

        private var backgroundColorName: String? = null;

        constructor(backgroundColor: String, layout: LayoutManager?) : super(layout) {
            this.backgroundColorName = backgroundColor;
        }

        override fun setBackground(bg: Color?) {
            if (this.backgroundColorName != null) {
                super.setBackground(UIManager.getColor(backgroundColorName))
            }
        }

    }

    class InputPanel : JPanel {

        private var backgroundColorName: String? = null;

        constructor(backgroundColorName: String) : super() {
            this.backgroundColorName = backgroundColorName;
        }

        override fun setBackground(bg: Color?) {
            if (this.backgroundColorName != null) {
                super.setBackground(UIManager.getColor(this.backgroundColorName))
            }
        }

    }

    private fun backgroundColorName(): String {
        return "ComboBox.background"
    }

    fun setItems(items: List<CheckBoxItem>) {
        checkBoxList.clear()

        items.forEach { item -> checkBoxList.addItem(item.text, item.text, item.selected) }

        refreshChosenItems()
    }

    val selectedItems: MutableList<String>
        get() {
            return checkBoxList.selectedItemTexts
        }

    private fun refreshChosenItems () {
        chosenItemsWrappedSubPanel.removeAll()

        if (checkBoxList.selectedItemTexts.isNotEmpty()) {
            checkBoxList.selectedItemTexts.forEach {
                val label = JLabel(it)
                label.background = null
                chosenItemsWrappedSubPanel.add(label)
            }
        }
        else {
            val notSelectedLabel = JLabel(MavenExecutorBundle.message("mavenExecutor.checkBoxCombo.notSelected.label"))
            notSelectedLabel.background = null
            chosenItemsWrappedSubPanel.add(notSelectedLabel)
        }

        chosenItemsWrappedSubPanel.updateUI()
        chosenItemsWrappedSubPanel.repaint()
        chosenItemsWrappedSubPanel.background = UIManager.getColor(backgroundColorName())
    }

    private fun showCheckBoxListPopup() {
        checkBoxList.border = EmptyBorder(10, 10, 10, 10)
        checkBoxList.updateUI()

        if (checkBoxListPopup == null || (checkBoxListPopup!!.isDisposed && !checkBoxListPopup!!.closeByAnchor)) {
            checkBoxListPopup = Popup(project, checkBoxList, chosenItemsWrappedSubPanel)
            checkBoxListPopup!!.setMinimumSize(Dimension(this.width, 30))
            checkBoxListPopup!!.component.background = UIManager.getColor("TextField.background")

            checkBoxListPopup!!.show(RelativePoint(this, Point(1, this.size.height)))
        }
        else {
            checkBoxListPopup = null
        }

    }

    interface SelectionChangedListener {
        fun selectionChanged(selectedItems: MutableList<String>)
    }

    class CheckBoxItem(val text: String, val selected: Boolean) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CheckBoxItem

            if (text != other.text) return false
            if (selected != other.selected) return false

            return true
        }

        override fun hashCode(): Int {
            var result = text.hashCode()
            result = 31 * result + selected.hashCode()
            return result
        }
    }

}