package io.github.jihch.handler;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.jihch.model.DownloadTask;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author jihch
 * 2022年5月3日
 */
@Component
@Primary
@Slf4j
public class MultithreadDownloader implements Downloader {

	@Override
	public void download(List<DownloadTask> taskList) {
		
		SSLContext ctx = null;
		try {
			ctx = SSLContextBuilder.create().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setSSLContext(ctx)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
		httpclient.start();

		int availableProcessors = Runtime.getRuntime().availableProcessors();
		int threadCount = availableProcessors << 1;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		AtomicInteger counter = new AtomicInteger(0);
		taskList.forEach(task -> {
			DownloaderRunnable r = new DownloaderRunnable(httpclient, task, counter, taskList.size());
			executorService.execute(r);
		});
		executorService.shutdown();

		while (true) {
			if (executorService.isTerminated()) {
				System.out.println("所有的子线程都结束了！");
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			httpclient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void download(DownloadTask... tasks) {
		List<DownloadTask> taskList = Arrays.asList(tasks);
		download(taskList);
	}

}
