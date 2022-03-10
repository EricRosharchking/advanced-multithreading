package com.liyuan.hong.advancedmultithreading;

import java.math.BigInteger;
import java.util.concurrent.*;

public class FactorialFJP extends CompletableFuture<BigInteger> {

    public static void main(String[] args) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ForkJoinFactorial f = new ForkJoinFactorial(1, 50);
        System.out.println(pool.invoke(f));
        pool.shutdown();
    }

}

class ForkJoinFactorial extends RecursiveTask<BigInteger> {
    private long leftBound;
    private long rightBound;
    private final int SHARD = 19;

    public ForkJoinFactorial(long leftBound, long rightBound) {
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }

    @Override
    protected BigInteger compute() {
        /*
        if
         */
        if (rightBound - leftBound <= SHARD) {
            return calculate();
        }
        if (rightBound < leftBound) {
            return BigInteger.ONE;
        }
        ForkJoinFactorial f1 = new ForkJoinFactorial(leftBound, (rightBound - leftBound) / 2 + leftBound);
        f1.fork();
        ForkJoinFactorial f2 = new ForkJoinFactorial((rightBound - leftBound) / 2 + leftBound + 1, rightBound);
        return f2.compute().multiply(f1.join());
    }

    private BigInteger calculate() {
        BigInteger res = BigInteger.ONE;
        for (long i = leftBound; i <= rightBound; i++) {
            res = res.multiply(BigInteger.valueOf(i));
        }
        return res;
    }
}