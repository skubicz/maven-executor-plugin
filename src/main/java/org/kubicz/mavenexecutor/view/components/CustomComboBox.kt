package org.kubicz.mavenexecutor.view.components

import com.intellij.openapi.ui.ComboBox
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicComboBoxRenderer

class CustomComboBox<E>(): ComboBox<E>() {

    override fun setRenderer(renderer: ListCellRenderer<in E>?) {
        if (renderer is BasicComboBoxRenderer.UIResource) {
            renderer.horizontalAlignment = SwingConstants.CENTER
        }

        super.setRenderer(renderer)
    }

}