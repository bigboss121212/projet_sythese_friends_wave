package com.antonin.friendswave.data.dataStructure

import com.antonin.friendswave.data.model.Event

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est une liste chainée qui permet de stocker des events, chaque event est un noeud et permet de faire des recherches plus rapides.

//Documentation https://www.kodeco.com/books/data-structures-algorithms-in-kotlin/v1.0/chapters/3-linked-list et avec l'aide de ChatGPT

class LinkedList {
    private var head: Node? = null
    private var tail: Node? = null

    inner class Node(val event: Event, var next: Node? = null)

    fun add(event: Event) {
        val newNode = Node(event)
        if (head == null) {
            head = newNode
            tail = newNode
        } else {
            tail?.next = newNode
            tail = newNode
        }
    }

    fun find(name: String): Event? {
        var current = head
        while (current != null) {
            if (current.event.name!!.contains(name) || current.event.name == name) {
                return current.event
            }
            current = current.next
        }
        return null
    }

    fun remove(name: String) {
        var current = head
        var previous: Node? = null
        while (current != null) {
            if (current.event.name == name) {
                if (previous == null) {
                    head = current.next
                } else {
                    previous.next = current.next
                }
                if (current == tail) {
                    tail = previous
                }
                return
            }
            previous = current
            current = current.next
        }
    }

    fun displayEvents() {
        var current = head

        while (current != null) {
            println("${current.event.name} (${current.event.date})")
            current = current.next
        }
    }

    fun mergeSort(head: Node?): Node? {
        if (head?.next == null) {
            return head
        }
        val middle = getMiddle(head)
        val nextToMiddle = middle?.next
        middle?.next = null

        val left = mergeSort(head)
        val right = mergeSort(nextToMiddle)

        return merge(left, right)
    }

    fun getMiddle(head: Node?): Node? {
        var slow = head
        var fast = head
        while (fast?.next != null && fast.next?.next != null) {
            slow = slow?.next
            fast = fast.next?.next
        }
        return slow
    }

    fun merge(left: Node?, right: Node?): Node? {
        if (left == null) {
            return right
        }
        if (right == null) {
            return left
        }
        val result: Node
        //tri en fonction de l'adresse
        if (left.event.adress!! <= right.event.adress.toString()) {
            result = left
            result.next = merge(left.next, right)
        } else {
            result = right
            result.next = merge(left, right.next)
        }
        return result
    }

}