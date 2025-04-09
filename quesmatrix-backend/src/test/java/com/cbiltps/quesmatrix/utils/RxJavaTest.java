package com.cbiltps.quesmatrix.utils;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lichengxiang
 * Date: 2025-04-08
 * Time: 14:51
 */
@SpringBootTest
public class RxJavaTest {
    @Test
    public void test() throws InterruptedException {
        // 创建数据流
        Flowable<Long> flowable = Flowable.interval(1, TimeUnit.SECONDS) // 每隔一秒发出一个数字
                .map(i -> i + 1)
                .subscribeOn(Schedulers.io()); // 指定在哪个线程池上发出数据

        // 订阅 Flowable 流
        flowable.observeOn(Schedulers.io()) // 指定在哪个线程池上执行下游操作
                .doOnNext(item -> System.out.println(item.toString())) // 打印每个接受到的数字
                .subscribe();

        Thread.sleep(10000L);
    }
}
