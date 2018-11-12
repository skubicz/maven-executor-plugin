package org.kubicz.mavenexecutor.model.settings

import com.intellij.util.xmlb.annotations.Property

class History {

    @Property
    private var maxItemsCount: Int = 20

    @Property
    var items: MutableList<String> = arrayListOf()

    fun add(item: String) {
        if (item.isBlank()) {
            return
        }

        if (!items.contains(item)) {
            items.add(0, item)
        }
        while (items.size > maxItemsCount) {
            items.removeAt(maxItemsCount)
        }
    }

    fun asArray(): Array<String> {
        return items.toTypedArray()
    }

}