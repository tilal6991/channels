package com.tilal6991.channels.redux.util

import com.github.andrewoma.dexx.collection.*
import com.tilal6991.channels.redux.state.Channel

class TransactingIndexedList<T> private constructor(
        private val actual: IndexedList<T>,
        val transactions: IndexedList<Transaction>,
        private val maxSize: Int,
        private val runningCount: Int) : IndexedList<T> by actual {

    fun transactionCount(): Int {
        return runningCount
    }

    fun maxSize(): Int {
        return maxSize
    }

    override fun drop(number: Int): TransactingIndexedList<T> {
        // TODO(tilal6991) what if number is greater than or equal to the size of the list.
        val list = if (number <= 0) transactions else maxAppend(transactions, Transaction(REMOVE, 0, -1, number))
        return TransactingIndexedList(actual.drop(number), list, maxSize, runningCount + 1)
    }

    override fun append(elem: T): TransactingIndexedList<T> {
        return TransactingIndexedList(actual.append(elem), maxAppend(transactions,
                Transaction(ADD, actual.size(), -1, 1)), maxSize, runningCount + 1)
    }

    override fun take(number: Int): TransactingIndexedList<T> {
        // TODO(tilal6991) what if number is greater than the less than or equal to zero.
        val list = if (number >= size()) transactions else maxAppend(transactions, Transaction(REMOVE, number, -1, size() - number))
        return TransactingIndexedList(actual.take(number), list, maxSize, runningCount + 1)
    }

    override fun range(from: Int, fromInclusive: Boolean, to: Int, toInclusive: Boolean): TransactingIndexedList<T> {
        val firstEnd = if (fromInclusive) from else from + 1
        val secondStart = if (toInclusive) to + 1 else to

        var rc = runningCount
        var list: IndexedList<Transaction> = transactions
        if (firstEnd != 0) {
            list = maxAppend(list, Transaction(REMOVE, 0, -1, firstEnd))
            rc++
        }
        if (secondStart != size()) {
            list = maxAppend(list, Transaction(REMOVE, secondStart, -1, size() - secondStart))
            rc++
        }

        return TransactingIndexedList(actual.range(from, fromInclusive, to, toInclusive), list, maxSize, rc)
    }

    override fun set(i: Int, elem: T): TransactingIndexedList<T> {
        if (get(i) === elem) {
            return this
        }

        return TransactingIndexedList(actual.set(i, elem),
                maxAppend(transactions, Transaction(CHANGE, i, -1, 1)), maxSize, runningCount + 1)
    }

    override fun prepend(elem: T): TransactingIndexedList<T> {
        return TransactingIndexedList(actual.prepend(elem), maxAppend(transactions,
                Transaction(ADD, 0, -1, 1)), maxSize, runningCount + 1)
    }

    fun addAt(elem: T, index: Int): TransactingIndexedList<T> {
        val list = IndexedLists.builder<T>()
                .addAll(actual.take(index) as Traversable<T>)
                .add(elem)
                .addAll(actual.drop(index) as Traversable<T>)
                .build()
        return TransactingIndexedList(list, maxAppend(transactions,
                Transaction(ADD, index, -1, 1)), maxSize, runningCount + 1)
    }

    fun removeAt(index: Int): TransactingIndexedList<T> {
        val list = IndexedLists.builder<T>()
                .addAll(actual.take(index) as Traversable<T>)
                .addAll(actual.drop(index + 1) as Traversable<T>)
                .build()
        return TransactingIndexedList(list, maxAppend(transactions,
                Transaction(REMOVE, index, -1, 1)), maxSize, runningCount + 1)
    }

    fun move(fromIndex: Int, toIndex: Int): TransactingIndexedList<T> {
        val builder = IndexedLists.builder<T>()
        if (fromIndex == toIndex) {
            return this
        } else if (fromIndex < toIndex) {
            builder.addAll(actual.take(fromIndex) as Traversable<T>)
                    .addAll(actual.range(fromIndex, false, toIndex, false) as Traversable<T>)
                    .add(actual.get(fromIndex))
                    .addAll(actual.range(toIndex, true, size(), false) as Traversable<T>)
        } else {
            builder.addAll(actual.take(toIndex) as Traversable<T>)
                    .add(actual.get(fromIndex))
                    .addAll(actual.range(toIndex, true, fromIndex, false) as Traversable<T>)
                    .addAll(actual.range(fromIndex, false, size(), false) as Traversable<T>)
        }

        return TransactingIndexedList(builder.build(), maxAppend(transactions,
                Transaction(MOVE, fromIndex, toIndex, 1)), maxSize, runningCount + 1)
    }

    private fun maxAppend(list: IndexedList<Transaction>, elem: Transaction): IndexedList<Transaction> {
        if (list.size() >= maxSize) {
            val i = list.size() - maxSize + 1
            return list.drop(i).append(elem)
        }
        return list.append(elem)
    }

    override fun toString(): String {
        return actual.joinToString(limit = 10)
    }

    class TransactingBuilder<T> internal constructor() : Builder<T, TransactingIndexedList<T>> {

        private var actualBuilder = IndexedLists.builder<T>()
        private var maxSize = DEFAULT_MAX_SIZE

        override fun add(element: T): Builder<T, TransactingIndexedList<T>> {
            actualBuilder = actualBuilder.add(element)
            return this
        }

        override fun addAll(elements: Traversable<T>): Builder<T, TransactingIndexedList<T>> {
            actualBuilder = actualBuilder.addAll(elements)
            return this
        }

        override fun addAll(elements: MutableIterable<T>): Builder<T, TransactingIndexedList<T>> {
            actualBuilder = actualBuilder.addAll(elements)
            return this
        }

        override fun addAll(iterator: MutableIterator<T>): Builder<T, TransactingIndexedList<T>> {
            actualBuilder = actualBuilder.addAll(iterator)
            return this
        }

        override fun addAll(e1: T, e2: T, vararg es: T): Builder<T, TransactingIndexedList<T>> {
            actualBuilder = actualBuilder.addAll(e1, e2, *es)
            return this
        }

        fun maxSize(size: Int) {
            maxSize = size
        }

        override fun build(): TransactingIndexedList<T> {
            return TransactingIndexedList(actualBuilder.build(), Vector.empty(), maxSize, 0)
        }
    }

    class Transaction(val type: Int, val startIndex: Int, val toIndex: Int, val count: Int)

    companion object {
        const val ADD = 1
        const val REMOVE = 2
        const val CHANGE = 3
        const val MOVE = 4
        const val DEFAULT_MAX_SIZE = 100

        fun <T> builder(): TransactingIndexedList.TransactingBuilder<T> {
            return TransactingIndexedList.TransactingBuilder()
        }

        fun <T> empty(): TransactingIndexedList<T> {
            return TransactingIndexedList(Vector.empty(), Vector.empty(), DEFAULT_MAX_SIZE, 0)
        }

        fun <T> wrapping(list: IndexedList<T>): TransactingIndexedList<T> {
            return TransactingIndexedList(list, Vector.empty(), DEFAULT_MAX_SIZE, 0)
        }
    }
}