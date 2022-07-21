package allpurpose.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Marco Sprenger, Livio Näf
 *
 * Scheduler to run only one task. If a new task is added, the old one is overwritten.
 * If the scheduler is in process, after a short delay the newest task is executes.
 * After execution the scheduler checks if a new task is available, if not the scheduler is paused.
 */
class Scheduler(private val delayInMillis: Long = 50L,
                private val scope : CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)) {


    private var inProcess = false
    private var scheduledTask: (() -> Unit)? = null

    private fun process() {
        if (inProcess) return
        if (scheduledTask == null) return

        scope.launch {  // is launched only if scope is "ready" to do the next task
            inProcess = true
            val taskToDo = scheduledTask

            delay(delayInMillis)

            if (taskToDo == scheduledTask) { //no new task was scheduled since 'delayInMillis'
                taskToDo?.invoke()
                scheduledTask = null
            }
            inProcess = false  //ready for next launch
            process()
        }
    }

    fun scheduleTask(task: () -> Unit) {
        scheduledTask = task
        process()
    }
}
