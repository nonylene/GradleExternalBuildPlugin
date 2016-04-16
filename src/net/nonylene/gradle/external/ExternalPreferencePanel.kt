package net.nonylene.gradle.external

import com.intellij.ide.DataManager
import com.intellij.ide.macro.MacroManager
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.FixedSizeButton
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.tools.ToolEditorDialog
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*
import javax.swing.text.BadLocationException


/**
 * @see ToolEditorDialog
 */
class ExternalPreferencePanel {

    private val provider = ServiceManager.getService(ExternalPreferenceProvider::class.java)

    private val insertMacroText = "Insert macro..."

    // command fields
    private val myTfCommandWorkingDirectory = JTextField(10)
    private val myTfCommand = JTextField(10)
    private val myParametersField = JTextField(10)
    private val myInsertWorkingDirectoryMacroButton = JButton(insertMacroText)
    private val myInsertCommandMacroButton = JButton(insertMacroText)
    private val myInsertParametersMacroButton = JButton(insertMacroText)

    private var project: Project? = null

    fun createPanel(): JPanel {
        val pane = JPanel(GridBagLayout())
        // program

        pane.add(JLabel("Program:"), GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            insets = Insets(0, 0, 0, 10)
            anchor = GridBagConstraints.BASELINE_LEADING
        })

        val browseCommandButton = FixedSizeButton(myTfCommand)

        addCommandBrowseAction(browseCommandButton, myTfCommand, myTfCommandWorkingDirectory)

        pane.add(JPanel(BorderLayout()).apply {
            add(myTfCommand, BorderLayout.CENTER)
            add(browseCommandButton, BorderLayout.EAST)
        }, GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            insets = Insets(0, 0, 0, 10)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING
            weightx = 1.0
        })

        pane.add(myInsertCommandMacroButton, GridBagConstraints().apply {
            gridx = 2
            gridy = 0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING
        })

        // parameters

        pane.add(JLabel("Parameters:"), GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            insets = Insets(5, 0, 0, 10)
            anchor = GridBagConstraints.BASELINE_LEADING
        })

        pane.add(myParametersField, GridBagConstraints().apply {
            gridx = 1
            gridy = 1
            insets = Insets(5, 0, 0, 10)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING
            weightx = 1.0
        })

        pane.add(myInsertParametersMacroButton, GridBagConstraints().apply {
            gridx = 2
            gridy = 1
            insets = Insets(5, 0, 0, 0)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING
        })

        pane.add(JTextArea("\$GRADLE_TASKS\$ and \$GRADLE_ARGS\$ will be replaced with Gradle Tasks and Arguments.").apply {
            lineWrap = true
            wrapStyleWord = true;
            lineWrap = true;
            isOpaque = false;
            isEditable = false;
            isFocusable = false;
        }, GridBagConstraints().apply {
            gridx = 1
            gridy = 2
            gridwidth = 3
            insets = Insets(5, 0, 0, 0)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING
        })

        // working directory

        pane.add(JLabel("Working Directory:"), GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            insets = Insets(5, 0, 0, 10)
            anchor = GridBagConstraints.BASELINE_LEADING
        })

        val browseDirectoryButton = FixedSizeButton(myTfCommandWorkingDirectory)
        addWorkingDirectoryBrowseAction(browseDirectoryButton, myTfCommandWorkingDirectory)

        pane.add(JPanel(BorderLayout()).apply {
            add(myTfCommandWorkingDirectory, BorderLayout.CENTER)
            add(browseDirectoryButton, BorderLayout.EAST)
        }, GridBagConstraints().apply {
            gridx = 1
            gridy = 3
            gridwidth = 1
            insets = Insets(5, 0, 0, 10)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 1.0
        })

        pane.add(myInsertWorkingDirectoryMacroButton, GridBagConstraints().apply {
            gridx = 2
            gridy = 3
            insets = Insets(5, 0, 0, 0)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING
        })

        // for normal resizing
        pane.add(JLabel(), GridBagConstraints().apply {
            gridy = 4
            fill = GridBagConstraints.BASELINE_LEADING
            weighty = 1.0
        })

        reset()

        return pane
    }

    init {
        val dataContext = DataManager.getInstance().dataContextFromFocus.result;
        MacroManager.getInstance().cacheMacrosPreview(dataContext)
        project = DataKeys.PROJECT.getData(dataContext)
        addListeners()
    }

    protected fun addWorkingDirectoryBrowseAction(browseDirectoryButton: FixedSizeButton, tfCommandWorkingDirectory: JTextField) {
        browseDirectoryButton.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            val chooser = FileChooserFactory.getInstance().createPathChooser(descriptor, project, tfCommandWorkingDirectory)

            chooser.choose(null, { files ->
                files.getOrNull(0)?.let {
                    tfCommandWorkingDirectory.text = it.presentableUrl
                }
            })
        }
    }

    protected fun addCommandBrowseAction(browseCommandButton: FixedSizeButton, tfCommand: JTextField, tfCommandWorkingDirectory: JTextField) {
        browseCommandButton.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
            val chooser = FileChooserFactory.getInstance().createPathChooser(descriptor, project, tfCommand)

            chooser.choose(null) { files ->
                files.getOrNull(0)?.let { file ->
                    tfCommand.text = file.presentableUrl
                    if (tfCommandWorkingDirectory.text.isNullOrEmpty()) {
                        file.parent?.let { parent ->
                            if (parent.isDirectory) tfCommandWorkingDirectory.text = parent.presentableUrl
                        }
                    }
                }
            }
        }
    }

    private inner class InsertMacroActionListener(private val myTextField: JTextField) : ActionListener {

        override fun actionPerformed(e: ActionEvent) {
            val dialog = MacrosDialog(project)
            if (dialog.showAndGet()) {
                dialog.selectedMacro?.let { macro ->
                    val position = myTextField.caretPosition
                    try {
                        myTextField.document.insertString(position, "$" + macro.name + "$", null)
                        myTextField.caretPosition = position + macro.name.length + 2
                    } catch (ignored: BadLocationException) {}
                }
            }
            IdeFocusManager.findInstance().requestFocus(myTextField, true)
        }
    }

    private fun addListeners() {
        myInsertCommandMacroButton.addActionListener(InsertMacroActionListener(myTfCommand))
        myInsertParametersMacroButton.addActionListener(InsertMacroActionListener(myParametersField))
        myInsertWorkingDirectoryMacroButton.addActionListener(InsertMacroActionListener(myTfCommandWorkingDirectory))
    }

    fun isModified(): Boolean {
        return provider.state?.let {
            it.workingDirectory != toSystemIndependentFormat(myTfCommandWorkingDirectory.text) ||
                    it.program != convertString(myTfCommand.text) ||
                    it.parameters != convertString(myParametersField.text)
        } ?: false
    }

    /**
     * Initialize controls
     */
    fun reset() {
        myTfCommandWorkingDirectory.text = toCurrentSystemFormat(provider.state?.workingDirectory)
        myTfCommand.text = provider.state?.program
        myParametersField.text = provider.state?.parameters
    }

    fun apply() {
        provider.state?.let {
            it.workingDirectory = toSystemIndependentFormat(myTfCommandWorkingDirectory.text)
            it.program = convertString(myTfCommand.text)
            it.parameters = convertString(myParametersField.text)
        }
    }

    private fun convertString(s: String?): String? {
        return if (s != null && s.trim().isEmpty()) null else s
    }

    private fun toSystemIndependentFormat(s: String?): String? {
        if (s == null) return null
        val trimmed  = s.trim()
        return if (!trimmed.isEmpty()) trimmed.replace(File.separatorChar, '/') else null
    }

    private fun toCurrentSystemFormat(s: String?): String? {
        if (s == null) return null
        val trimmed  = s.trim()
        return if (!trimmed.isEmpty()) trimmed.replace('/', File.separatorChar) else null
    }
}