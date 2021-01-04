package clue.observer

interface Observer<T> {
    fun notify(newState : T)
}