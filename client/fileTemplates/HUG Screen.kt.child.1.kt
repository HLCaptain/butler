#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
#parse("File Header.java")
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class ${NAME}ScreenModel(
    // Inject your dependencies
) : ScreenModel {
    private val dataFlow1 = MutableStateFlow(false)
    private val dataFlow2 = MutableStateFlow(true)

    val state = combine(
        dataFlow1,
        dataFlow2
    ) { flow1, flow2 ->
        ${NAME}State(
            dataFlow1 = flow1,
            dataFlow2 = flow2
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ${NAME}State(
            dataFlow1 = dataFlow1.value,
            dataFlow2 = dataFlow2.value
        )
    )

    fun setDataFlow1(state: Boolean) {
        // Use IO dispatcher if Voyager crashes unexpectedly
        screenModelScope.launch {
            dataFlow1.update { state }
        }
    }
}