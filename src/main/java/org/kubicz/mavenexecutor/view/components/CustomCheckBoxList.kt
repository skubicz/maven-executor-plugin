package org.kubicz.mavenexecutor.view.components

import java.util.ArrayList

import javax.swing.JCheckBox

import com.intellij.ui.CheckBoxList

class CustomCheckBoxList : CheckBoxList<String>() {

    val selectedItemTexts: MutableList<String>
        get() {
            val selectedItems = ArrayList<String>()

            val model = model ?: return selectedItems

            for (i in 0 until model.size) {
                val checkBox = model.getElementAt(i) as JCheckBox
                if (checkBox.isSelected) {
                    selectedItems.add(checkBox.text)
                }
            }

            return selectedItems
        }

    val selectedItemNames: MutableList<String>
        get() {
            val selectedItems = ArrayList<String>()

            val model = model ?: return selectedItems

            for (i in 0 until model.size) {
                val checkBox = model.getElementAt(i) as JCheckBox
                if (checkBox.isSelected) {
                    selectedItems.add(checkBox.name)
                }
            }

            return selectedItems
        }

    fun addItemWithValue(item: String, text: String, value: String, selected: Boolean) {
        addItem(item, text, selected)
        model.getElementAt(itemsCount - 1).name = value
    }
}