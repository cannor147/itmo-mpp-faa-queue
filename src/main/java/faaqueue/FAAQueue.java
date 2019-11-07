package faaqueue;

import kotlinx.atomicfu.*;

import static faaqueue.FAAQueue.Node.NODE_SIZE;


public class FAAQueue<T> implements Queue<T> {
    private static final Object DONE = new Object(); // Marker for the "DONE" slot state; to avoid memory leaks

    private AtomicRef<Node> head; // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private AtomicRef<Node> tail; // Tail pointer, similarly to the Michael-Scott queue

    public FAAQueue() {
        Node firstNode = new Node();
        head = new AtomicRef<>(firstNode);
        tail = new AtomicRef<>(firstNode);
    }

    @Override
    public void enqueue(T x) {
        int enqIdx = this.tail.getValue().enqIdx++;
        if (enqIdx >= NODE_SIZE) {
            Node newTail = new Node(x);
            this.tail.getValue().next = newTail;
            this.tail.setValue(newTail);
            return;
        }
        this.tail.getValue().data[enqIdx] = x;
    }

    @Override
    public T dequeue() {
        while (true) {
            if (this.head.getValue().isEmpty()) {
                if (this.head.getValue().next == null) return null;
                this.head.setValue(this.head.getValue().next);
                continue;
            }
            int deqIdx = this.head.getValue().deqIdx++;
            Object res = head.getValue().data[deqIdx];
            head.getValue().data[deqIdx] = DONE;
            return (T) res;
        }
    }

    static class Node {
        static final int NODE_SIZE = 2; // CHANGE ME FOR BENCHMARKING ONLY

        private Node next = null;
        private int enqIdx = 0; // index for the next enqueue operation
        private int deqIdx = 0; // index for the next dequeue operation
        private final Object[] data = new Object[NODE_SIZE];

        Node() {}

        Node(Object x) {
            this.enqIdx = 1;
            this.data[0] = x;
        }

        private boolean isEmpty() {
            return this.deqIdx >= this.enqIdx || this.deqIdx >= NODE_SIZE;
        }
    }
}