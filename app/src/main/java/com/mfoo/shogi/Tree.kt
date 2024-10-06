package com.mfoo.shogi

sealed interface Tree<T> {
    val children: List<Node<T>>

    data class RootNode<T>(
        override val children: List<Node<T>>,
    ) : Tree<T> {
        override fun toString(): String {
            return "Root node: \n${children}"
        }
    }

    // Be permissive and allow moves after a game termination
    data class Node<T>(
        override val children: List<Node<T>>,
        val value: T,
    ) : Tree<T> {
        override fun toString(): String {
            return "${value} ${children.map { "\n Child of ${value} -- ${it}" }}"
        }
    }


    fun <R> fold(acc: R, f: (R, Tree<T>) -> R): R {
        return this.children
            .fold(f(acc, this)) { a, n -> n.fold(a, f) }
    }

    /**
     * Tree catamorphism.
     */
    fun <R> cata(f: (tree: Tree<T>, convertedSubTree: Collection<R>) -> R): R {
        return f(this, this.children.map { it.cata(f) })
    }
}
