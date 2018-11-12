package org.kubicz.mavenexecutor.view.components

import java.awt.Color
import javax.swing.JButton
import javax.swing.UIManager

class CustomButton(text: String) : JButton(text) {

    private var factor = 0.8

    fun overrideBackground(selected: Boolean) {
        putClientProperty("JButton.backgroundColor", color(selected))

        repaint()
    }

    private fun color(selected: Boolean): Color {
        var defaultColor = UIManager.getColor("Button.background")
        return if(selected) {
            if(calculateColorLuminance(defaultColor) < 0.5) {
                brighter(defaultColor)
            }
            else {
                darker(defaultColor)
            }
        } else {
            defaultColor
        }
    }

    private fun calculateColorLuminance(color: Color): Double {
        return calculateLuminanceContribution(color.red / 255.0) * 0.2126 +
                calculateLuminanceContribution(color.green / 255.0) * 0.7152 +
                calculateLuminanceContribution(color.blue / 255.0) * 0.0722
    }

    private fun calculateLuminanceContribution(colorValue: Double): Double {
        return if (colorValue <= 0.03928) {
            colorValue / 12.92
        } else Math.pow((colorValue + 0.055) / 1.055, 2.4)
    }

    private fun darker(color: Color): Color {
        return Color(Math.max((color.red * 0.8).toInt(), 0),
                Math.max((color.green * 0.8).toInt(), 0),
                Math.max((color.blue * 0.8).toInt(), 0),
                color.alpha)
    }

    private fun brighter(color: Color): Color {
        var r = color.red
        var g = color.green
        var b = color.blue
        val alpha = color.alpha

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        val i = (1.0 / (1.0 - factor)).toInt()
        if (r == 0 && g == 0 && b == 0) {
            return Color(i, i, i, alpha)
        }
        if (r in 1..(i - 1)) r = i
        if (g in 1..(i - 1)) g = i
        if (b in 1..(i - 1)) b = i

        return Color(Math.min((r / factor).toInt(), 255),
                Math.min((g / factor).toInt(), 255),
                Math.min((b / factor).toInt(), 255),
                alpha)
    }
}
