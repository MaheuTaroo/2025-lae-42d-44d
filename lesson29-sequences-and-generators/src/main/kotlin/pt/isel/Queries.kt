package pt.isel

fun <T, R> Iterable<T>.eagerMap(transform: (T) -> R): List<R> {
    val destination = mutableListOf<R>()
    for (item in this)
        destination.add(transform(item))
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

fun <T, R> Sequence<T>.suspMap(transform: (T) -> R) = sequence {
    for (item in this@suspMap)
        yield(transform(item))
}

fun <T> Sequence<T>.suspFilter(predicate: (T) -> Boolean) = sequence {
    for (item in this@suspFilter) {
        if (predicate(item)) {
            yield(item)
        }
    }
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

fun <T> Sequence<T>.lazyDistinct() =
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

fun <T> Sequence<T>.lazyConcat(other: Sequence<T>) =
    object : Sequence<T> {
        override fun iterator() =
            object : Iterator<T> {
                val base = this@lazyConcat.iterator()
                val args = other.iterator()

                override fun hasNext() =
                    base.hasNext() || args.hasNext()

                override fun next() =
                    if (base.hasNext())
                        base.next()
                    else
                        args.next()
            }
    }

fun <T> Sequence<T>.suspConcat(other: Sequence<T>) = sequence {
    yieldAll(this@suspConcat)
    yieldAll(other)
}

fun <T : Any?> Sequence<T>.collapse() = sequence {
    val iter = this@collapse.iterator()
    if (!iter.hasNext())
        return@sequence

    var curr = iter.next()
    yield(curr)

    for (i in this@collapse) {
        if (i == curr)
            continue

        curr = i
        yield(i)
    }
}

fun <T> Sequence<T>.suspDistinct() = sequence {
    val set = mutableSetOf<T>()
    for (it in this@suspDistinct) {
        if (set.add(it))
            yield(it)
    }
}

fun <T, R, V> Sequence<T>.suspZip(other: Sequence<R>, transform: (a: T, b: R) -> V) = sequence<V> {
    val base = this@suspZip.iterator()
    val secondary = other.iterator()

    while (base.hasNext() && secondary.hasNext())
        yield(transform(base.next(), secondary.next()))
}

fun <T : Any?> Sequence<T>.lazyCollapse() =
    object : Sequence<T> {
        override fun iterator() =
            object : Iterator<T> {
                val iter = this@lazyCollapse.iterator()
                var traversed = true
                var latest = iter.next()

                override fun hasNext(): Boolean {
                    if (traversed) {
                        return iter.hasNext()
                    }

                    if (!iter.hasNext())
                        return false

                    while (iter.hasNext()) {
                        val curr = iter.next()

                        if (curr != latest) {
                            
                            return true
                        }
                    }
                    return false
                }

                override fun next() =
                    items.last()
            }

    }
