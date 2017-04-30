package com.whe.test;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.RandomAccess;

public class MyList<E> implements Serializable, RandomAccess, Cloneable, Iterable<E> {
    private Object[] elementData;
    private int defaultCapacity = 8;
    private int size = 0;

    public MyList() {
        elementData = new Object[defaultCapacity];
    }

    public MyList(int initCapacity) {
        if (initCapacity > 0) {
            elementData = new Object[initCapacity];
        } else if (initCapacity < 0) {
            throw new IllegalArgumentException();
        } else {
            elementData = new Object[defaultCapacity];
        }
    }

    public boolean add(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        elementData[size++] = e;
        if (elementData.length == size) {
            grow();
        }
        return true;
    }

    private void grow() {
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity << 1;
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    public int size() {
        return size;
    }

    public E remove(int index) {
        if (index < 0 || index > elementData.length) {
            throw new IndexOutOfBoundsException();
        }
        E oldValue = (E) elementData[index];
        int numMoved = size - index - 1;
        //arraycopy(源数组,源数组起始位置,目标数组,目标数组起始位置,复制长度)
        System.arraycopy(elementData, index + 1, elementData, index,
                numMoved);
        elementData[--size] = null;
        return oldValue;
    }

    public E get(int index) {
        if (index < 0 || index > elementData.length) {
            throw new IndexOutOfBoundsException();
        }
        return (E) elementData[index];
    }


    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int currentSize = 0;

            @Override
            public boolean hasNext() {
                return !(size == 0 || currentSize >= size);
            }

            @Override
            public E next() {
                if (currentSize >= size) {
                    throw new IndexOutOfBoundsException();
                }
                return (E) elementData[currentSize++];
            }
        };
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < size; i++) {
            str += elementData[i] + ",";
        }
        return str;
    }
}
