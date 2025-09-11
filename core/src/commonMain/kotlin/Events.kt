package io.ktor.chat

interface ObservableRepository<E: Identifiable<ID>, ID>: Repository<E, ID> {
    fun onChange(observer: Observer<E>): Observer<E>
    fun forget(observer: Observer<E>)
}

fun <E: Identifiable<ID>, ID> Repository<E, ID>.observable(onFailure: (Exception) -> Unit = {}): ObservableRepository<E, ID> =
    WatchListObservableRepository(this, onFailure)

typealias Observer<E> = suspend (ChangeType, E) -> Unit

enum class ChangeType {
    CREATE,
    UPDATE,
    DELETE,
}

internal class WatchListObservableRepository<E: Identifiable<ID>, ID>(
    private val delegate: Repository<E, ID>,
    private val onFailure: (Exception) -> Unit,
): Repository<E, ID> by delegate, ObservableRepository<E, ID> {

    private var observers = listOf<suspend (ChangeType, E) -> Unit>()

    override fun onChange(observer: Observer<E>): Observer<E> {
        observers += observer
        return observer
    }

    override fun forget(observer: Observer<E>) {
        observers -= observer
    }

    override suspend fun create(e: E): E =
        delegate.create(e).also {
            for (observer in observers)
                observer.create(it)
        }

    override suspend fun update(e: E) {
        delegate.update(e).also {
            for (observer in observers)
                observer.update(e)
        }
    }

    override suspend fun delete(id: ID) {
        val e = get(id) ?: return
        delegate.delete(id).also {
            for (observer in observers)
                observer.delete(e)
        }
    }

    private suspend fun Observer<E>.create(e: E) = tryObserve(this, ChangeType.CREATE, e)
    private suspend fun Observer<E>.update(e: E) = tryObserve(this, ChangeType.UPDATE, e)
    private suspend fun Observer<E>.delete(e: E) = tryObserve(this, ChangeType.DELETE, e)

    private suspend fun tryObserve(observer: Observer<E>, changeType: ChangeType, e: E) {
        try {
            observer(changeType, e)
        } catch (ex: Exception) {
            onFailure(ex)
            forget(observer)
        }
    }

}