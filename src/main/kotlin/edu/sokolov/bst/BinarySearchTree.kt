package edu.sokolov.bst

import java.util.*

// attention: Comparable is supported but Comparator is not
open class BinarySearchTree<T : Comparable<T>> : AbstractMutableSet<T>(), SortedSet<T> {

    protected class Node<T>(
        val value: T
    ) {
        var left: Node<T>? = null
        var right: Node<T>? = null
    }

    protected open var root: Node<T>? = null

    private var _size = 0

    override val size
        get() = _size


    private fun find(value: T): Node<T>? =
        root?.let { find(it, value) }

    private fun find(start: Node<T>, value: T): Node<T> {
        val comparison = value.compareTo(start.value)
        return when {
            comparison == 0 -> start
            comparison < 0 -> start.left?.let { find(it, value) } ?: start
            else -> start.right?.let { find(it, value) } ?: start
        }
    }

    override operator fun contains(element: T): Boolean {
        val closest = find(element)
        return closest != null && element.compareTo(closest.value) == 0
    }

    /**
     * Добавление элемента в дерево
     *
     * Если элемента нет в множестве, функция добавляет его в дерево и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     *
     * Спецификация: [java.util.Set.add] (Ctrl+Click по add)
     *
     */
    override fun add(element: T): Boolean {
        val closest = find(element)
        val comparison = if (closest == null) -1 else element.compareTo(closest.value)
        if (comparison == 0) {
            return false
        }
        val newNode = Node(element)
        when {
            closest == null -> root = newNode
            comparison < 0 -> {
                assert(closest.left == null)
                closest.left = newNode
            }
            else -> {
                assert(closest.right == null)
                closest.right = newNode
            }
        }
        _size++
        return true
    }

    /**
     * Удаление элемента из дерева
     *
     * Если элемент есть в множестве, функция удаляет его из дерева и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     * Высота дерева не должна увеличиться в результате удаления.
     *
     * Спецификация: [java.util.Set.remove] (Ctrl+Click по remove)
     * (в Котлине тип параметера изменён с Object на тип хранимых в дереве данных)
     *
     */
    override fun remove(element: T): Boolean =
        /*
        time complexity: average: O(log(n)), worst: O(n) for growing/decreasing only set
        space complexity: O(n)
     */
        root?.removeNode(element)?.also { root = it.first; if (it.second) _size-- }?.second ?: false


    /**
     * finds and removes element [value] from the tree beginning with this node
     * @return node to be replaced instead of removable and true if element was removed
     */
    private fun Node<T>.removeNode(value: T): Pair<Node<T>?, Boolean> {
        if (value < this.value) {
            return this to (this.left?.removeNode(value)?.also { this.left = it.first }?.second ?: false)
        }
        if (value > this.value) {
            return this to (this.right?.removeNode(value)?.also { this.right = it.first }?.second ?: false)
        }

        // node to be removed was found

        // case where node hasn't children
        if (this.left == null && this.right == null) {
            return null to true
        }

        // case where node has only right child
        if (this.left == null) {
            return this.right to true
        }
        // case where node has only left child
        if (this.right == null) {
            return this.left to true
        }

        // case where node has both children
        val minNode = this.right!!.minNode()

        return Node(minNode.value).also {
            it.left = this.left
            it.right = this.right!!.removeNode(minNode.value).first
        } to true
    }

    /**
     * @return node with the lowest value beginning this node
     */
    private tailrec fun Node<T>.minNode(): Node<T> {
        return if (this.left == null)
            this
        else
            this.left!!.minNode()
    }


    override fun comparator(): Comparator<in T>? =
        null

    override fun iterator(): MutableIterator<T> {
        return BinarySearchTreeIterator(lowerBound = false, upperBound = false)
    }

    inner class BinarySearchTreeIterator internal constructor(
        private val lowerBound: Boolean,
        private val fromElement: T? = null,
        private val upperBound: Boolean,
        private val toElementExclusively: T? = null,
    ) : MutableIterator<T> {

        private val iterationStack = Stack<Node<T>>()
        private val iterationParentsStack = Stack<Node<T>>()

        init {
            root?.fillIterationStack()
        }

        private fun Node<T>.fillIterationStack() {
            // push all leftmost nodes
            var current: Node<T>? = this

            while (current != null) {
                if ((!lowerBound || fromElement!! <= current.value) && (!upperBound || current.value < toElementExclusively!!)) {
                    iterationStack.push(current)
                    iterationParentsStack.push(current)
                }
                if (lowerBound && current.value >= fromElement!!)
                    current = current.left
                else if (upperBound && current.value < toElementExclusively!!)
                    current = current.right
                else if (!lowerBound && !upperBound)
                    current = current.left
            }
        }

        /**
         * #parentOfCurrentNode property is needed for handling of nodes replacement in iterator remove
         */
        private val parentOfCurrentNode: Node<T>?
            get() = if (iterationParentsStack.empty()) null else iterationParentsStack.peek()
        private var currentNode: Node<T>? = null

        /**
         * Проверка наличия следующего элемента
         *
         * Функция возвращает true, если итерация по множеству ещё не окончена (то есть, если вызов next() вернёт
         * следующий элемент множества, а не бросит исключение); иначе возвращает false.
         *
         * Спецификация: [java.util.Iterator.hasNext] (Ctrl+Click по hasNext)
         *
         */
        override fun hasNext(): Boolean = iterationStack.isNotEmpty()
                || currentNode?.right != null
                && (!lowerBound || currentNode!!.right!!.hasLeftNodesInBound())

        private fun Node<T>.hasLeftNodesInBound(): Boolean {
            var curr: Node<T>? = this

            while (curr != null) {
                if ((!lowerBound || curr.value >= fromElement!!) && (!upperBound || curr.value < toElementExclusively!!))
                    return true
                curr = curr.left
            }
            return false
        }


        /**
         * Получение следующего элемента
         *
         * Функция возвращает следующий элемент множества.
         * Так как BinarySearchTree реализует интерфейс SortedSet, последовательные
         * вызовы next() должны возвращать элементы в порядке возрастания.
         *
         * Бросает NoSuchElementException, если все элементы уже были возвращены.
         *
         * Спецификация: [java.util.Iterator.next] (Ctrl+Click по next)
         *
         */
        override fun next(): T {
            currentNode?.right?.let {
                iterationParentsStack.push(currentNode)
            }

            currentNode?.right?.fillIterationStack()

            try {
                currentNode = iterationStack.pop()

                while (iterationParentsStack.contains(currentNode))
                    iterationParentsStack.pop()

            } catch (e: EmptyStackException) {
                throw NoSuchElementException("All elements of the set was already returned!")
            }

            return currentNode!!.value
        }

        /**
         * Удаление предыдущего элемента
         *
         * Функция удаляет из множества элемент, возвращённый крайним вызовом функции next().
         *
         * Бросает IllegalStateException, если функция была вызвана до первого вызова next() или же была вызвана
         * более одного раза после любого вызова next().
         *
         * Спецификация: [java.util.Iterator.remove] (Ctrl+Click по remove)
         *
         */
        override fun remove() = currentNode?.let {
            it.removeNode(it.value).also { (newNode, successfulRemoval) ->
                parentOfCurrentNode?.let { parentNode ->
                    if (parentNode.value < it.value)
                        parentNode.right = newNode
                    else
                        parentNode.left = newNode
                } ?: run { root = newNode }

                if (currentNode?.right != null && currentNode?.left != null) {
                    iterationStack.push(newNode)
                    iterationParentsStack.push(newNode)
                } else if (currentNode?.right != null) {
                    currentNode!!.right?.fillIterationStack()
                }

                if (successfulRemoval) _size-- else throw ConcurrentModificationException()
            }

            currentNode = null
        } ?: throw IllegalStateException("Current element does not exist!")

    }

    override fun subSet(fromElement: T, toElement: T): SortedSet<T> = TODO()
    override fun headSet(toElement: T): SortedSet<T> = TODO()
    override fun tailSet(fromElement: T): SortedSet<T> = TODO()

    override fun first(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.left != null) {
            current = current.left!!
        }
        return current.value
    }

    override fun last(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.right != null) {
            current = current.right!!
        }
        return current.value
    }

}