package faaqueue;

import kotlinx.atomicfu.*;

import static faaqueue.FAAQueue.Node.NODE_SIZE;


public class FAAQueue<T> implements Queue<T> {
    private static final Object DONE = new Object();

    private AtomicRef<Node> head;
    private AtomicRef<Node> tail;

    public FAAQueue() {
        Node firstNode = new Node();
        head = new AtomicRef<>(firstNode);
        tail = new AtomicRef<>(firstNode);
    }

    @Override
    public void enqueue(T x) {
        while (true) {
            Node tail = this.tail.getValue();
            int enqIndex = tail.enqIdx.getAndIncrement();
            if (enqIndex >= NODE_SIZE) {
                Node newTail = new Node(x);
                if (tail.next.compareAndSet(null, newTail)) {
                    this.tail.setValue(newTail);
                    return;
                }
            } else if (tail.data.get(enqIndex).compareAndSet(null, x)) {
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T dequeue() {
        while (true) {
            Node head = this.head.getValue();
            if (head.isEmpty()) {
                Node headNext = head.next.getValue();
                if (headNext == null) {
                    return null;
                } else {
                    this.head.compareAndSet(head, headNext);
                }
            } else {
                int deqIndex = head.deqIdx.getAndIncrement();
                if (deqIndex >= NODE_SIZE) {
                    continue;
                }
                Object res = head.data.get(deqIndex).getAndSet(DONE);
                if (res == null) {
                    continue;
                }
                return (T) res;
            }
        }
    }

    static class Node {
        static final int NODE_SIZE = 2;

        private AtomicRef<Node> next = new AtomicRef<>(null);
        private AtomicInt enqIdx = new AtomicInt(0);
        private AtomicInt deqIdx = new AtomicInt(0);
        private final AtomicArray<Object> data = new AtomicArray<>(NODE_SIZE);

        Node() {}

        Node(Object x) {
            this.enqIdx = new AtomicInt(1);
            this.data.get(0).setValue(x);
        }

        private boolean isEmpty() {
            return this.deqIdx.getValue() >= this.enqIdx.getValue() || this.deqIdx.getValue() >= NODE_SIZE;
        }
    }
}