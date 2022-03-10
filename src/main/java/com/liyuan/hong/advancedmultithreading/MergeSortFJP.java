package com.liyuan.hong.advancedmultithreading;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MergeSortFJP {
    public static void main(String[] args) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        int[] arr =
//                IntStream.iterate(20, (x) -> x = x - 1)
                new Random().ints()
                .limit(20000).toArray();
//        System.out.println(Arrays.toString(arr));
        ForkJoinMergeSort f = new ForkJoinMergeSort(arr, 0, arr.length - 1);
        pool.invoke(f);
        System.out.println(isSorted(arr));
        pool.shutdown();
    }

    private static boolean isSorted(int[] arr) {
        for (int i = arr.length - 1; i > arr.length; i--) {
            if (arr[i] < arr[i-1]) {
                return false;
            }
        }
        return true;
    }
}

class ForkJoinMergeSort extends RecursiveAction {

    private int[] array;
    private int start, end;

    public ForkJoinMergeSort(int[] arr, int start, int end) {
        array = arr;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        if (start == end) {
            return;
        }
        int mid = (end - start) / 2 + start;
        if (end - start <= 17) {
            sort(start, mid);
            sort(mid + 1, end);
            merge(start, mid, end);
            return;
        }
        ForkJoinMergeSort f1 = new ForkJoinMergeSort(array, start, mid);
        ForkJoinMergeSort f2 = new ForkJoinMergeSort(array, mid + 1, end);
        invokeAll(f1, f2);
        merge(start, mid, end);
    }

    private void merge(int start, int mid, int end) {
        int[] helperArr = Arrays.copyOfRange(array, start, end+1);
        int leftLengh = mid - start + 1, rightLength = end - start + 1;
//        System.out.println(Arrays.toString(helperArr));
        int leftIndex = 0, rightIndex = mid - start + 1, helperIndex = start;
        while (leftIndex < leftLengh && rightIndex < rightLength) {
            if (helperArr[rightIndex] < helperArr[leftIndex]) {
                array[helperIndex++] = helperArr[rightIndex++];
            } else {
                array[helperIndex++] = helperArr[leftIndex++];
            }
        }
        while (leftIndex < leftLengh) {
            array[helperIndex++] = helperArr[leftIndex++];
        }
        while (rightIndex < rightLength) {
            array[helperIndex++] = helperArr[rightIndex++];
        }
//        System.out.println(Arrays.toString(array));
    }

    private void sort(int start, int end) {
        for (int i = start; i < end; i++) {
            for (int j = end; j > i; j--) {
                if (array[j] < array[i]) {
                    insert(i, j);
                }
            }
        }
    }

    private void insert(int position, int index) {
        for (int i = index; i > position; i--) {
            swap(i - 1, i);
        }
    }

    private void swap(int position, int index) {
        array[position] = array[position] + array[index];
        array[index] = array[position] - array[index];
        array[position] = array[position] - array[index];
    }
}