package org.kubicz.mavenexecutor.view.window.panels

import com.google.common.collect.Lists
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Pair
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.*
import com.intellij.ui.TextFieldWithAutoCompletion.StringsCompletionProvider
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBScrollBar
import com.intellij.util.LocalTimeCounter
import com.intellij.util.TextFieldCompletionProvider
import com.intellij.util.textCompletion.TextCompletionUtil
import com.intellij.util.textCompletion.TextFieldWithCompletion
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import org.jetbrains.idea.maven.execution.MavenArgumentsCompletionProvider
import org.kubicz.mavenexecutor.model.settings.History
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Point
import java.awt.event.*
import javax.swing.*

class AutoCompletionExpandableTextFieldWithHistory @JvmOverloads constructor(private val project: Project,
                                                                             private val completionProvider: TextFieldCompletionProvider,
                                                                             private val text: String,
                                                                             private val history: History) : BorderLayoutPanel(), Expandable {
    private val expandButton = JLabel(AllIcons.General.ExpandComponent)
    private var expandedPopup: JBPopup? = null
    private val expandedTextField: TextFieldWithCompletion
    private val comboBox: ComboBox<String>
    private var comboBoxEditor: ComboBoxEditorWithAutoCompletion = ComboBoxEditorWithAutoCompletion(project)
    var changedListener: (text: String) -> Unit = {}
    var focusListener: () -> Unit = {}


    init {
        expandButton.toolTipText = KeymapUtil.createTooltipText("Expand", "CustomExpandExpandableComponent")
        expandButton.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        expandButton.border = JBUI.Borders.empty(0, 3)
        expandButton.disabledIcon = AllIcons.General.ExpandComponent
        object : ClickListener() {
            override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
                expand()
                return true
            }
        }.installOn(expandButton)
        expandButton.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                expandButton.icon = AllIcons.General.ExpandComponentHover
            }

            override fun mouseExited(e: MouseEvent) {
                expandButton.icon = AllIcons.General.ExpandComponent
            }
        })

        expandedTextField = object : TextFieldWithCompletion(project, MavenArgumentsCompletionProvider(project), "", true, false, false) {
            override fun createEditor(): EditorEx {
                val editor = super.createEditor()
                editor.colorsScheme.editorFontName = font.fontName
                editor.colorsScheme.editorFontSize = font.size
                editor.contentComponent.border = JBUI.Borders.empty(5)
                editor.scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                editor.scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
                editor.scrollPane.verticalScrollBar.isOpaque = true
                editor.settings.isUseSoftWraps = true
                copyCaretPosition(comboBoxEditor.editorTextField.editor, editor)
                addCollapseButton(editor, Runnable { collapse() })

                editor.backgroundColor = UIManager.getColor(background(isEnabled))

                return editor
            }
        }

        completionProvider.apply(expandedTextField)

        expandedTextField.setOneLineMode(false)
        expandedTextField.setPreferredWidth(40)
        expandedTextField.setBackground(UIManager.getColor(background(isEnabled)))
        setExpandedPopupSize(300, 200)

        comboBox = ComboBox(history.asArray())

        comboBox.setMinimumAndPreferredWidth(100)
        comboBox.isEditable = true
        comboBox.editor = comboBoxEditor
        comboBox.renderer = EditorComboBoxRenderer(comboBoxEditor)
        comboBox.maximumRowCount = 10

        comboBoxEditor.editorTextField.text = text;

        comboBox.addActionListener() {
            comboBoxEditor.editorTextField.text = (it.source as ComboBox<String>).selectedItem as String
        }
        addToCenter(comboBox)


        comboBoxEditor.editorTextField.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                history.add(comboBoxEditor.editorTextField.text)

                comboBox.model = DefaultComboBoxModel<String>(history.asArray())
            }
        })

        comboBoxEditor.editorTextField.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                changedListener(comboBoxEditor.editorTextField.text)
            }
        })

    }

    fun setText(text: String) {
        comboBoxEditor.editorTextField.text = text
    }

    private fun setExpandedPopupSize(width: Int, height: Int) {
        expandedTextField.minimumSize = Dimension(width, height)
        expandedTextField.size = Dimension(width, height)
        expandedTextField.preferredSize = Dimension(width, height)
    }

    private fun createExpandedPopup() {
        expandedPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(expandedTextField, null)
                .setMayBeParent(true)
                .setFocusable(true)
                .setResizable(true)
                .setMinSize(Dimension(comboBox.width, 200))
                .setRequestFocus(true)
                .setLocateByContent(true)
                .setShowBorder(false)
                .setCancelOnWindowDeactivation(false)
                .setKeyboardActions(listOf(Pair.create(ActionListener { event: ActionEvent? -> collapse() }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK))))
                .setCancelCallback {
                    copyFromPopupToCombo()
                    IdeFocusManager.getInstance(project).requestFocus(comboBoxEditor.editorTextField, true)
                    expandedPopup = null
                    true
                }.createPopup()

    }

    private fun addCollapseButton(editor: EditorEx, handler: Runnable) {
        ErrorStripeEditorCustomization.DISABLED.customize(editor)
        // TODO: copied from ExpandableTextField
        val pane = editor.scrollPane
        pane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        pane.verticalScrollBar.add(JBScrollBar.LEADING, object : JLabel(AllIcons.General.CollapseComponent) {
            init {
                toolTipText = KeymapUtil.createTooltipText("Collapse", "CollapseExpandableComponent")
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                border = JBUI.Borders.empty(5, 0, 5, 5)
                addMouseListener(object : MouseAdapter() {
                    override fun mouseEntered(event: MouseEvent) {
                        icon = AllIcons.General.CollapseComponentHover
                    }

                    override fun mouseExited(event: MouseEvent) {
                        icon = AllIcons.General.CollapseComponent
                    }

                    override fun mousePressed(event: MouseEvent) {
                        handler.run()
                    }
                })
            }
        })
    }

    inner class InputBorderLayoutPanel: BorderLayoutPanel() {
        override fun setBackground(bg: Color?) {
            super.setBackground(UIManager.getColor(background(isEnabled)))
        }
    }

     private fun addExpand(component: EditorTextField): JComponent {
        val panel = InputBorderLayoutPanel()

        panel.addToCenter(component)
        panel.addToRight(expandButton)

        return panel
    }

    override fun collapse() {
        expandedPopup!!.cancel()
        expandedPopup = null
    }

    override fun isExpanded(): Boolean {
        return expandedPopup != null && expandedPopup!!.isVisible
    }

    override fun expand() {
        copyFromComboToPopup()
        if (expandedPopup == null) {
            createExpandedPopup()
        }
        expandedPopup!!.show(RelativePoint(comboBox, Point(0, 0)))
    }

    private fun background(enable: Boolean): String {
        return if (isEnabled) "TextField.background" else "ComboBox.disabledBackground"
    }

    private fun copyCaretPosition(source: Editor?, destination: Editor?) {
        if (source != null && destination != null) {
            destination.caretModel.moveToOffset(source.caretModel.offset)
        }
    }

    private fun copyFromComboToPopup() {
        expandedTextField.text = comboBoxEditor.editorTextField.text.replace("\\n".toRegex(), " ")
        copyCaretPosition(comboBoxEditor.editorTextField.editor, expandedTextField.editor)
    }

    private fun copyFromPopupToCombo() {
        comboBoxEditor.editorTextField.text = expandedTextField.text.replace("\\n".toRegex(), " ")
        copyCaretPosition(expandedTextField.editor, comboBoxEditor.editorTextField.editor)
    }

    private inner class ComboBoxEditorWithAutoCompletion internal constructor(project: Project?) : ComboBoxEditor {
        private val myPanel: JComponent
        private val myDelegate: EditorComboBoxEditor
        val editorTextField: EditorTextField
            get() = myDelegate.editorComponent

        init {
            myDelegate = object : EditorComboBoxEditor(project, PlainTextFileType.INSTANCE) {
                override fun onEditorCreate(editor: EditorEx) {
                    super.onEditorCreate(editor)
                    editor.setBorder(JBUI.Borders.empty(0, 0))

                    editor.backgroundColor = UIManager.getColor(if (isEnabled) "TextField.background" else "ComboBox.disabledBackground")
                    editor.colorsScheme.editorFontName = font.fontName
                    editor.colorsScheme.editorFontSize = font.size
                    copyCaretPosition(expandedTextField.editor, editor)
                }

            }
            myDelegate.editorComponent.setFontInheritedFromLAF(false)
            myDelegate.editorComponent.border = JBUI.Borders.empty(0, 0)

            var comp = addExpand(myDelegate.editorComponent)

            project?.let {
                completionProvider.apply(editorTextField)
            }

            myPanel = comp
        }

        override fun getEditorComponent(): JComponent {
            return myPanel
        }

        override fun setItem(anObject: Any?) {}

        override fun getItem(): String { //  Object document = this.myDelegate.getItem();
            return (this.myDelegate.item as Document).text// TODO document instanceof Document ? XDebuggerExpressionComboBox.this.getEditorsProvider().createExpression(XDebuggerExpressionComboBox.this.getProject(), (Document)document, XDebuggerExpressionComboBox.this.myExpression.getLanguage(), XDebuggerExpressionComboBox.this.myExpression.getMode()) : null;
        }

        override fun selectAll() {
            myDelegate.selectAll()
        }

        override fun addActionListener(l: ActionListener) {
            myDelegate.addActionListener(l)
        }

        override fun removeActionListener(l: ActionListener) {
            myDelegate.removeActionListener(l)
        }
    }

    private fun createDocument(text: String): Document? {
        val factory = PsiFileFactory.getInstance(project)
        val fileType = PlainTextLanguage.INSTANCE.associatedFileType
        val stamp = LocalTimeCounter.currentTime()
        val psiFile = factory.createFileFromText("Dummy." + fileType!!.defaultExtension, fileType, text, stamp, true, false)
        val p = StringsCompletionProvider(Lists.newArrayList("temp3"), AllIcons.General.ArrowDown)



        psiFile.putUserData(TextCompletionUtil.COMPLETING_TEXT_FIELD_KEY, p)
        psiFile.putUserData(TextCompletionUtil.AUTO_POPUP_KEY, true)
        return PsiDocumentManager.getInstance(project).getDocument(psiFile)
    }
}