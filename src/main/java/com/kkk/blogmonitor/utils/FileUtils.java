package com.kkk.blogmonitor.utils;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * File文件相关工具方法
 * Created by z3jjlzt on 2018/7/21.
 */
@Slf4j
public class FileUtils {

    public static String SUFFIX_STR = "_old";

    /**
     * 得到指定目录下所有文件，包含子目录
     *
     * @param path 文件路径
     * @return 文件路径下的所有文件集合，如果目录不存在
     * 抛出异常
     */
    public static List<File> getAllFiles(String path) {
        File[] files = new File(path).listFiles();
        List<File> fileList = Arrays.stream(Optional.ofNullable(files).orElseThrow(RuntimeException::new))
                .flatMap(x -> x.isDirectory() ? Arrays.stream(x.listFiles()) : Stream.of(x))
                .collect(Collectors.toList());
        System.out.println(fileList);
        return fileList;
    }

    public static void fileMonitor(String dir) throws IOException {
        Path path = Paths.get(dir);
        WatchService watchService = Optional.ofNullable(path)
                .orElseThrow(IOException::new)
                .getFileSystem()
                .newWatchService();
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
        Executors.newSingleThreadExecutor().execute(() -> {
            log.info("博客监控系统已经启动......");
            try {
                while (true) {
                    WatchKey watchKey = watchService.poll(60, TimeUnit.SECONDS);
                    if (watchKey != null) {
                        for (WatchEvent event : watchKey.pollEvents()) {
                            //解决新建文件时报文件被其他进程打开的异常
                            Thread.sleep(500);
                            WatchEvent<Path> pe = event;
                            Path uri = (Path) watchKey.watchable();
                            String fullpath = uri.toString() + "/" + pe.context().getFileName();
                            File file = new File(fullpath);
                            String fileMD5 = HashCodeUtil.getFileMD5(file);
                            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                if (!RedisUtil.exists(fullpath + SUFFIX_STR)) {
                                    RedisUtil.set(fullpath + SUFFIX_STR,fileMD5);
                                    log.info("保存初始版本......{} : {}",fullpath + SUFFIX_STR,fileMD5);
                                }
                                if (!Optional.ofNullable(RedisUtil.get(fullpath + SUFFIX_STR)).orElse("")
                                        .equals(fileMD5)) {
                                    log.info("更新版本......{} : {}",fullpath,fileMD5);
                                    RedisUtil.set(fullpath, fileMD5);
                                }
                            } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                RedisUtil.set(fullpath + SUFFIX_STR, fileMD5);
                                log.info("保存初始版本......{} : {}",fullpath + SUFFIX_STR,fileMD5);
                            } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                RedisUtil.del(fullpath + SUFFIX_STR);
                                RedisUtil.del(fullpath);
                                log.info("删除初始版本......{} : {}",fullpath + SUFFIX_STR,fileMD5);
                                log.info("删除更新版本......{} : {}",fullpath,fileMD5);
                            }
                        }
                        if (!watchKey.reset()) {
                            //已经关闭了进程
                            log.info("博客监控系统已经关闭......");
                            RedisUtil.close();
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
