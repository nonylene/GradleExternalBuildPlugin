package net.nonylene.gradle.external

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class ExternalPreference : SearchableConfigurable {

    var preferencePanel: ExternalPreferencePanel? = ExternalPreferencePanel()

    override fun getHelpTopic(): String? {
        return null
    }

    override fun getDisplayName(): String? {
        return "Gradle External Build"
    }

    override fun enableSearch(p0: String?): Runnable? {
        return null
    }

    override fun getId(): String {
        return "net.nonylene.gradle.external.preference"
    }

    override fun reset() {
        preferencePanel?.reset()
    }

    override fun isModified(): Boolean {
        return preferencePanel?.isModified() ?: false
    }

    override fun disposeUIResources() {
        preferencePanel = null
    }

    override fun apply() {
        preferencePanel?.apply()
    }

    override fun createComponent(): JComponent? {
        return preferencePanel?.createPanel()
    }
}

