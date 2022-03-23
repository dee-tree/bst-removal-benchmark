package edu.sokolov.bst.impl

import edu.sokolov.bst.AbstractBinarySearchTreeBenchmark
import edu.sokolov.bst.SetState

@Suppress("unused")
open class BSTAllElementsRemovalIteratorBenchmark : AbstractBinarySearchTreeBenchmark() {

    override fun doWork(setState: SetState<Int>) {
        val iterator = setState.set.iterator()

        while (iterator.hasNext()) {
            iterator.next()
            iterator.remove()
        }
    }

}