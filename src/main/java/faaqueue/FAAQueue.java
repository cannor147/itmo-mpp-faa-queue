package faaqueue;

import static faaqueue.FAAQueue.Node.NODE_SIZE;


public class FAAQueue<T> implements Queue<T> {
    private static final Object DONE = new Object(); // Marker for the "DONE" slot state; to avoid memory leaks

    private Node head; // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private Node tail; // Tail pointer, similarly to the Michael-Scott queue

    public FAAQueue() {
        head = tail = new Node();
    }

    @Override
    public void enqueue(T x) {
        int enqIdx = this.tail.enqIdx++;
        if (enqIdx >= NODE_SIZE) {
            Node newTail = new Node(x);
            this.tail.next = newTail;
            this.tail = newTail;
            return;
        }
        this.tail.data[enqIdx] = x;
    }

    @Override
    public T dequeue() {
        while (true) {
            if (this.head.isEmpty()) {
                if (this.head.next == null) return null;
                this.head = this.head.next;
            }
            int deqIdx = this.head.deqIdx++;
            if (deqIdx >= NODE_SIZE) continue;
            Object res = head.data[deqIdx];
            head.data[deqIdx] = DONE;
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
            return this.deqIdx >= this.enqIdx;
        }
    }
}