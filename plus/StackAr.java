// 4. A stack of ints on an array: implement it and write its specification.
//   ./esc plus/StackAr.java
// The fields are spec_public: they are private, but the public contracts may mention them.
public class StackAr {

    /** Stack capacity. */
    private static final /*@ spec_public @*/ int DEFAULT_CAPACITY = 10;

    /** Array of elements on the stack. */
    private final /*@ spec_public @*/ int[] elems;

    /** Index of the top element (-1 when the stack is empty). */
    private /*@ spec_public @*/ int top = -1;

    // TODO (4): class invariant
    //@ public invariant elems != null;
    //@ public invariant -1 <= top && top < elems.length;

    // TODO (4): specify every constructor and method
    //@ ensures elems.length == DEFAULT_CAPACITY;
    //@ ensures top == -1;
    public StackAr() {
        this(DEFAULT_CAPACITY);
    }

    //@ requires capacity >= 0;
    //@ ensures elems.length == capacity;
    //@ ensures top == -1; 
    public StackAr(int capacity) {
        elems = new int[capacity];
    }

    //@ ensures \result <==> top == -1;
    public /*@ pure @*/ boolean isEmpty() {
        return top == -1;
    }

    //@ ensures \result <==> top == elems.length - 1;
    public /*@ pure @*/ boolean isFull() {
        return (top == elems.length-1);
    }

    //@ ensures \result == top + 1;
    public /*@ pure @*/ int size() {
        return (top + 1);
    }

    //@ requires top < elems.length - 1;
    //@ assignable top, elems[*];
    //@ ensures top == \old(top) + 1;
    //@ ensures elems[top] == o;
    public void push(int o) {
        top ++;
        elems[top] = o;
    }

    //@ requires top >= 0;
    //@ assignable top;
    //@ ensures top == \old(top) - 1;
    //@ ensures \result == elems[\old(top)];
    public int pop() {
        // we write somehow redundant but solid version
        int elem_to_pop = elems[top];
        top --; // no need to actually remove the top element
        return elem_to_pop;       
    }

    //@ requires top >= 0;
    //@ ensures \result == elems[top];
    public /*@ pure @*/ int peek() {
        return elems[top];
    }
}
