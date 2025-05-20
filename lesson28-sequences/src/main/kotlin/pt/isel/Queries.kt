package pt.isel

fun <T, R> Iterable<T>.eagerMap(transform: (T) -> R): List<R> {
    val destination = mutableListOf<R>()
    // for (item in this)
    //    destination.add(transform(item))
    val iter = this.iterator()
    while (iter.hasNext()) {
        destination.add(transform(iter.next()))
    }
    return destination
}

fun <T> Iterable<T>.eagerFilter(predicate: (T) -> Boolean): Iterable<T> {
    val destination = mutableListOf<T>()
    for (item in this) {
        if (predicate(item)) {
            destination.add(item)
        }
    }
    return destination
}

fun <T> Iterable<T>.eagerDistinct(): Iterable<T> {
    val destination = mutableSetOf<T>()
    for (item in this) {
        destination.add(item)
    }
    return destination
}

fun <T, R> Sequence<T>.lazyMap(transform: (T) -> R): Sequence<R> =
    object : Sequence<R> {
        override fun iterator(): Iterator<R> =
            object : Iterator<R> {
                val iter = this@lazyMap.iterator()

                override fun hasNext() = iter.hasNext()

                override fun next() = transform(iter.next())
            }
    }

/**
 * DOES NOT SUPPORT Sequences with null elements
 */
fun <T> Sequence<T>.lazyDistinct(): Sequence<T> =
    object : Sequence<T> {
        override fun iterator() =
            object : Iterator<T> {
                val iter = this@lazyDistinct.iterator()
                var next = iter.next()
                val set = mutableSetOf(next)
                var traversed = true

                override fun hasNext(): Boolean {
                    if (traversed)
                        return iter.hasNext()

                    traversed = true
                    while (iter.hasNext() && !set.add(next))
                        next = iter.next()

                    return iter.hasNext()
                }

                override fun next(): T {
                    if (traversed) {
                        traversed = false

                        return next
                    }

                    if (!hasNext()) throw NoSuchElementException()
                    return next
                }
            }
    }

fun <T> Sequence<T>.lazyConcat(vararg elems: T) =
    object : Sequence<T> {
        override fun iterator() =
            object : Iterator<T> {
                val base = this@lazyConcat.iterator()
                val args = elems.iterator()

                override fun hasNext() =
                    base.hasNext() || args.hasNext()

                override fun next() =
                    if (base.hasNext())
                        base.next()
                    else
                        args.next()
            }
    }