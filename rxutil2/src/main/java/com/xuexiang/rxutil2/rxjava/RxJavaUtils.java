/*
 * Copyright (C) 2018 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xuexiang.rxutil2.rxjava;

import android.support.annotation.NonNull;

import com.xuexiang.rxutil2.rxjava.task.RxAsyncTask;
import com.xuexiang.rxutil2.rxjava.task.RxIOTask;
import com.xuexiang.rxutil2.rxjava.task.RxIteratorTask;
import com.xuexiang.rxutil2.rxjava.task.RxUITask;
import com.xuexiang.rxutil2.subsciber.BaseSubscriber;
import com.xuexiang.rxutil2.subsciber.SimpleThrowableAction;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * RxJava工具
 *
 * @author xuexiang
 * @date 2018/3/4 上午1:20
 */
public final class RxJavaUtils {

    private final static String TAG = "RxJavaUtils";

    //========================线程任务==========================//

    /**
     * 在ui线程中工作
     *
     * @param uiTask 在UI线程中操作的任务
     * @param <T>
     * @return
     */
    public static <T> Disposable doInUIThread(@NonNull RxUITask<T> uiTask) {
        return doInUIThread(uiTask, new SimpleThrowableAction(TAG));
    }

    /**
     * 在ui线程中工作
     *
     * @param uiTask        在UI线程中操作的任务
     * @param errorConsumer 出错的处理
     * @param <T>
     * @return
     */
    public static <T> Disposable doInUIThread(@NonNull RxUITask<T> uiTask, @NonNull Consumer<Throwable> errorConsumer) {
        return Flowable.just(uiTask)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RxUITask<T>>() {
                    @Override
                    public void accept(RxUITask<T> rxUITask) throws Exception {
                        rxUITask.doInUIThread(rxUITask.getInData());
                    }
                }, errorConsumer);
    }

    /**
     * 在IO线程中执行任务
     *
     * @param ioTask 在io线程中操作的任务
     * @param <T>
     * @return
     */
    public static <T> Disposable doInIOThread(@NonNull RxIOTask<T> ioTask) {
        return doInIOThread(ioTask, new SimpleThrowableAction(TAG));
    }

    /**
     * 在IO线程中执行任务
     *
     * @param ioTask        在io线程中操作的任务
     * @param errorConsumer 出错的处理
     * @param <T>
     * @return
     */
    public static <T> Disposable doInIOThread(@NonNull RxIOTask<T> ioTask, @NonNull Consumer<Throwable> errorConsumer) {
        return Flowable.just(ioTask)
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<RxIOTask<T>>() {
                    @Override
                    public void accept(RxIOTask<T> rxIOTask) throws Exception {
                        rxIOTask.doInIOThread(rxIOTask.getInData());
                    }
                }, errorConsumer);
    }
    //========================轮询操作==========================//

    /**
     * 轮询操作
     *
     * @param interval 轮询间期
     * @param consumer 监听事件
     */
    public static Disposable polling(long interval, @NonNull Consumer<Long> consumer) {
        return polling(0, interval, consumer);
    }

    /**
     * 轮询操作
     *
     * @param initialDelay 初始延迟
     * @param interval     轮询间期
     * @param consumer     监听事件
     */
    public static Disposable polling(long initialDelay, long interval, @NonNull Consumer<Long> consumer) {
        return polling(initialDelay, interval, TimeUnit.SECONDS, consumer, new SimpleThrowableAction(TAG));
    }

    /**
     * 轮询操作
     *
     * @param initialDelay  初始延迟
     * @param interval      轮询间期
     * @param unit          轮询间期时间单位
     * @param consumer      监听事件
     * @param errorConsumer 出错的事件
     */
    public static Disposable polling(long initialDelay, long interval, TimeUnit unit, @NonNull Consumer<Long> consumer, @NonNull Consumer<Throwable> errorConsumer) {
        return Flowable.interval(initialDelay, interval, unit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer, errorConsumer);
    }

    //========================延迟操作==========================//

    /**
     * 延迟操作
     *
     * @param delayTime 延迟时间
     * @param consumer  监听事件
     */
    public static Disposable delay(long delayTime, @NonNull Consumer<Long> consumer) {
        return delay(delayTime, TimeUnit.SECONDS, consumer, new SimpleThrowableAction(TAG));
    }

    /**
     * 延迟操作
     *
     * @param delayTime     延迟时间
     * @param unit          延迟时间单位
     * @param consumer      监听事件
     * @param errorConsumer 出错的事件
     */
    public static Disposable delay(long delayTime, TimeUnit unit, @NonNull Consumer<Long> consumer, @NonNull Consumer<Throwable> errorConsumer) {
        return Flowable.timer(delayTime, unit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer, errorConsumer);
    }

    //=====================AsyncTask=========================//

    /**
     * 执行Rx通用任务 (IO线程中执行耗时操作 执行完成调用UI线程中的方法)
     *
     * @param rxTask 执行任务
     * @param <T>
     * @return
     */
    public static <T, R> Disposable executeAsyncTask(@NonNull RxAsyncTask<T, R> rxTask) {
        return executeAsyncTask(rxTask, new SimpleThrowableAction(TAG));
    }

    /**
     * 执行Rx通用任务 (IO线程中执行耗时操作 执行完成调用UI线程中的方法)
     *
     * @param rxTask        执行任务
     * @param errorConsumer 出错的处理
     * @param <T>
     * @return
     */
    public static <T, R> Disposable executeAsyncTask(@NonNull RxAsyncTask<T, R> rxTask, @NonNull Consumer<Throwable> errorConsumer) {
        RxTaskOnSubscribe<RxAsyncTask<T, R>> onSubscribe = getRxAsyncTaskOnSubscribe(rxTask);
        return Flowable.create(onSubscribe, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RxAsyncTask<T, R>>() {
                    @Override
                    public void accept(RxAsyncTask<T, R> rxAsyncTask) throws Exception {
                        rxAsyncTask.doInUIThread(rxAsyncTask.getOutData());  //在UI线程工作
                    }
                }, errorConsumer);
    }

    @NonNull
    private static <T, R> RxTaskOnSubscribe<RxAsyncTask<T, R>> getRxAsyncTaskOnSubscribe(@NonNull final RxAsyncTask<T, R> rxTask) {
        return new RxTaskOnSubscribe<RxAsyncTask<T, R>>(rxTask) {
            @Override
            public void subscribe(FlowableEmitter<RxAsyncTask<T, R>> emitter) throws Exception {
                RxAsyncTask<T, R> task = getTask();
                task.setOutData(task.doInIOThread(task.getInData()));  //在io线程工作
                emitter.onNext(task);
                emitter.onComplete();
            }
        };
    }

    /**
     * 执行异步任务（IO线程处理，UI线程显示）
     *
     * @param t     处理入参
     * @param func1 动作
     * @return
     */
    public static <T, R> Flowable<R> executeAsyncTask(@NonNull T t, @NonNull Function<T, R> func1) {
        return Flowable.just(t)
                .map(func1)
                .compose(RxSchedulerUtils.<R>_io_main_f());
    }


    /**
     * 执行异步任务（IO线程处理，UI线程显示）
     *
     * @param t             处理入参
     * @param func1         动作
     * @param consumer      监听事件
     * @return
     */
    public static <T, R> Disposable executeAsyncTask(@NonNull T t, @NonNull Function<T, R> func1, @NonNull Consumer<R> consumer) {
        return executeAsyncTask(t, func1).subscribe(consumer, new SimpleThrowableAction(TAG));
    }


    /**
     * 执行异步任务（IO线程处理，UI线程显示）
     *
     * @param t             处理入参
     * @param func1         动作
     * @param consumer      监听事件
     * @param errorConsumer 出错的事件
     * @return
     */
    public static <T, R> Disposable executeAsyncTask(@NonNull T t, @NonNull Function<T, R> func1, @NonNull Consumer<R> consumer, @NonNull Consumer<Throwable> errorConsumer) {
        return executeAsyncTask(t, func1).subscribe(consumer, errorConsumer);
    }


    /**
     * 执行异步任务（IO线程处理，UI线程显示）
     *
     * @param t           处理入参
     * @param transformer 转化器
     * @return
     */
    public static <T, R> Flowable<R> executeAsyncTask(@NonNull T t, @NonNull FlowableTransformer<T, R> transformer) {
        return Flowable.just(t)
                .compose(transformer)
                .compose(RxSchedulerUtils.<R>_io_main_f());
    }

    /**
     * 执行异步任务（IO线程处理，UI线程显示）
     *
     * @param t             处理入参
     * @param transformer   转化器
     * @param consumer      监听事件
     * @return
     */
    public static <T, R> Disposable executeAsyncTask(@NonNull T t, @NonNull FlowableTransformer<T, R> transformer, @NonNull Consumer<R> consumer) {
        return executeAsyncTask(t, transformer).subscribe(consumer, new SimpleThrowableAction(TAG));
    }

    /**
     * 执行异步任务（IO线程处理，UI线程显示）
     *
     * @param t             处理入参
     * @param transformer   转化器
     * @param consumer      监听事件
     * @param errorConsumer 出错的事件
     * @return
     */
    public static <T, R> Disposable executeAsyncTask(@NonNull T t, @NonNull FlowableTransformer<T, R> transformer, @NonNull Consumer<R> consumer, @NonNull Consumer<Throwable> errorConsumer) {
        return executeAsyncTask(t, transformer).subscribe(consumer, errorConsumer);
    }


    //=====================集合、数组遍历处理=========================//

    /**
     * 遍历集合进行处理（IO线程处理，UI线程显示）
     *
     * @param rxIteratorTask
     * @return
     */
    public static <T, R> Disposable executeRxIteratorTask(final RxIteratorTask<T, R> rxIteratorTask) {
        return executeRxIteratorTask(rxIteratorTask, new SimpleThrowableAction(TAG));
    }


    /**
     * 遍历集合进行处理（IO线程处理，UI线程显示）
     *
     * @param rxIteratorTask
     * @param errorConsumer  出错的处理
     * @return
     */
    public static <T, R> Disposable executeRxIteratorTask(final RxIteratorTask<T, R> rxIteratorTask, @NonNull Consumer<Throwable> errorConsumer) {
        Flowable<T> flowable = rxIteratorTask.isArray() ? Flowable.fromArray(rxIteratorTask.getArray()) : Flowable.fromIterable(rxIteratorTask.getIterable());
        return flowable.map(new Function<T, R>() {
            @Override
            public R apply(T t) throws Exception {
                return rxIteratorTask.doInIOThread(t);
            }
        }).compose(RxSchedulerUtils.<R>_io_main_f())
                .subscribe(new Consumer<R>() {
                    @Override
                    public void accept(R r) throws Exception {
                        rxIteratorTask.doInUIThread(r);
                    }
                }, errorConsumer);
    }

    /**
     * 遍历数组进行处理（IO线程处理，UI线程显示）
     *
     * @param t             数组
     * @param func1         动作
     * @param consumer      监听事件
     * @return
     */
    public static <T, R> Disposable foreach(@NonNull T[] t, @NonNull Function<T, R> func1, @NonNull Consumer<R> consumer) {
        return foreach(t, func1, consumer, new SimpleThrowableAction(TAG));
    }


    /**
     * 遍历数组进行处理（IO线程处理，UI线程显示）
     *
     * @param t             数组
     * @param func1         动作
     * @param consumer      监听事件
     * @param errorConsumer 出错的事件
     * @return
     */
    public static <T, R> Disposable foreach(@NonNull T[] t, @NonNull Function<T, R> func1, @NonNull Consumer<R> consumer, @NonNull Consumer<Throwable> errorConsumer) {
        return Flowable.fromArray(t)
                .map(func1)
                .compose(RxSchedulerUtils.<R>_io_main_f())
                .subscribe(consumer, errorConsumer);
    }


    /**
     * 遍历数组进行处理（IO线程处理，UI线程显示）
     *
     * @param t             数组
     * @param transformer   转化器
     * @param consumer      监听事件
     * @return
     */
    public static <T, R> Disposable foreach(@NonNull T[] t, @NonNull FlowableTransformer<T, R> transformer, @NonNull Consumer<R> consumer) {
        return foreach(t, transformer, consumer, new SimpleThrowableAction(TAG));
    }


    /**
     * 遍历数组进行处理（IO线程处理，UI线程显示）
     *
     * @param t             数组
     * @param transformer   转化器
     * @param consumer      监听事件
     * @param errorConsumer 出错的事件
     * @return
     */
    public static <T, R> Disposable foreach(@NonNull T[] t, @NonNull FlowableTransformer<T, R> transformer, @NonNull Consumer<R> consumer, @NonNull Consumer<Throwable> errorConsumer) {
        return Flowable.fromArray(t)
                .compose(transformer)
                .compose(RxSchedulerUtils.<R>_io_main_f())
                .subscribe(consumer, errorConsumer);
    }


    /**
     * 遍历集合进行处理（IO线程处理，UI线程显示）
     *
     * @param t             数组
     * @param func1         动作
     * @param consumer      监听事件
     * @return
     */
    public static <T, R> Disposable foreach(@NonNull Iterable<T> t, @NonNull Function<T, R> func1, @NonNull Consumer<R> consumer) {
        return foreach(t, func1, consumer, new SimpleThrowableAction(TAG));
    }


    /**
     * 遍历集合进行处理（IO线程处理，UI线程显示）
     *
     * @param t             数组
     * @param func1         动作
     * @param consumer      监听事件
     * @param errorConsumer 出错的事件
     * @return
     */
    public static <T, R> Disposable foreach(@NonNull Iterable<T> t, @NonNull Function<T, R> func1, @NonNull Consumer<R> consumer, @NonNull Consumer<Throwable> errorConsumer) {
        return Flowable.fromIterable(t)
                .map(func1)
                .compose(RxSchedulerUtils.<R>_io_main_f())
                .subscribe(consumer, errorConsumer);
    }


    /**
     * 遍历集合进行处理（IO线程处理，UI线程显示）
     *
     * @param t             数组
     * @param transformer   转化器
     * @param consumer      监听事件
     * @return
     */
    public static <T, R> Disposable foreach(@NonNull Iterable<T> t, @NonNull FlowableTransformer<T, R> transformer, @NonNull Consumer<R> consumer) {
        return foreach(t, transformer, consumer, new SimpleThrowableAction(TAG));
    }

    /**
     * 遍历集合进行处理（IO线程处理，UI线程显示）
     *
     * @param t             数组
     * @param transformer   转化器
     * @param consumer      监听事件
     * @param errorConsumer 出错的事件
     * @return
     */
    public static <T, R> Disposable foreach(@NonNull Iterable<T> t, @NonNull FlowableTransformer<T, R> transformer, @NonNull Consumer<R> consumer, @NonNull Consumer<Throwable> errorConsumer) {
        return Flowable.fromIterable(t)
                .compose(transformer)
                .compose(RxSchedulerUtils.<R>_io_main_f())
                .subscribe(consumer, errorConsumer);
    }


}
