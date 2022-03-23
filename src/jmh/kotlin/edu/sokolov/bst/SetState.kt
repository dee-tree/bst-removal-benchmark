package edu.sokolov.bst

interface SetState <T : Comparable<T>> {
    val set: BinarySearchTree<T>
    val controlList: MutableList<T>
    val sortedControlList: MutableList<T>

    val randomElementIterator: MutableIterator<T>
    val minElementIterator: MutableIterator<T>
    val maxElementIterator: MutableIterator<T>

    val min: T
    val max: T
    val random: T
}