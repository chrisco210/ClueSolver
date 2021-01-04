package clue.observer

interface Observable<T> {
    fun register(observer : Observer<T>)
}