package edu.sokolov.bst

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
abstract class AbstractBinarySearchTreeBenchmark {

    @State(Scope.Thread)
    abstract class BenchmarkSetState<T : Comparable<T>> : SetState<T> {

        @Param("5", "10", "50", "100", "500", "1000")
//        @Param("100")
        var size: Int = 0

        companion object {
            val range = 1..100
        }

        override val set = BinarySearchTree<T>()
        override val controlList = mutableListOf<T>()
        override val sortedControlList = mutableListOf<T>()

        private lateinit var data: List<T>

        override lateinit var min: T
        override lateinit var max: T
        override lateinit var random: T

        override lateinit var randomElementIterator: MutableIterator<T>
        override lateinit var minElementIterator: MutableIterator<T>
        override lateinit var maxElementIterator: MutableIterator<T>


        @Setup(Level.Trial)
        fun initData() {
            data = generateData()

            min = data.minOf { it }
            random = data.random()
            max = data.maxOf { it }

        }

        abstract fun generateData(): List<T>


        @Setup(Level.Invocation)
        fun updateCollections() {
            controlList.addAll(data)

            sortedControlList.addAll(controlList)
            sortedControlList.sort()

            set.addAll(data)

            minElementIterator = set.iterator()
            minElementIterator.next()

            randomElementIterator = set.iterator()
            while (randomElementIterator.next() != random) ;

            maxElementIterator = set.iterator()
            while (maxElementIterator.hasNext())
                maxElementIterator.next()

        }

        @TearDown(Level.Invocation)
        fun clear() {
            controlList.clear()
            sortedControlList.clear()
            set.clear()
        }

    }

    abstract fun doWork(setState: SetState<Int>)

    @Benchmark
    @Fork(1)
    @Suppress("unused")
    fun doWorkRandomSetState(state: RandomSetState) {
        doWork(state)
    }

    @Benchmark
    @Fork(1)
    @Suppress("unused")
    fun doWorkAscendingSetState(state: AscendingSetState) {
        doWork(state)
    }

    @Benchmark
    @Fork(1)
    @Suppress("unused")
    fun doWorkDescendingSetState(state: DescendingSetState) {
        doWork(state)
    }


    @State(Scope.Thread)
    open class AscendingSetState : BenchmarkSetState<Int>() {

        override fun generateData(): List<Int> {
            var base = 0

            val list = ArrayList<Int>(size)

            repeat(size) {
                base += range.random()
                list.add(base)
            }

            return list
        }
    }

    @State(Scope.Thread)
    open class DescendingSetState : BenchmarkSetState<Int>() {

        override fun generateData(): List<Int> {
            var base = 10_000_000

            val list = ArrayList<Int>(size)

            repeat(size) {
                base -= range.random()
                list.add(base)
            }

            return list
        }
    }

    @State(Scope.Thread)
    open class RandomSetState : BenchmarkSetState<Int>() {
        override fun generateData(): List<Int> {
            return List(size) { Random.nextInt() }.shuffled()
        }
    }
}
