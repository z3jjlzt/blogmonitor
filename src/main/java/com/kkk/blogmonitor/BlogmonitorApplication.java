package com.kkk.blogmonitor;

import com.kkk.blogmonitor.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class BlogmonitorApplication {

	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(BlogmonitorApplication.class, args);
        FileUtils.fileMonitor("f://test/");
	}
}
