package net.nonylene.gradle.external

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.State
import com.intellij.openapi.components.StoragePathMacros

@State(name = "ExternalPreferenceProvider", storages = arrayOf(Storage(file = "${StoragePathMacros.APP_CONFIG}/externalBuild.xml")))
class ExternalPreferenceProvider : PersistentStateComponent<ExternalPreferenceProvider.State> {

    class State {
        var workingDirectory: String? = null
        var program: String? = null
        var parameters: String? = null
    }

    private var state: State? = State()

    override fun getState(): State? {
        return state
    }

    override fun loadState(state: State?) {
        this.state = state
    }
}
