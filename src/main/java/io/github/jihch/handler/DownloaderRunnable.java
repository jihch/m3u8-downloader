package io.github.jihch.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import io.github.jihch.model.DownloadTask;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author jihch
 * 2022年5月3日
 */
@Slf4j
public class DownloaderRunnable implements Runnable {
	private final CloseableHttpAsyncClient closeableHttpAsyncClient;
	private final DownloadTask downloadTask;
	private final AtomicInteger count;
	private final int total;
	private static final String FORMAT = "%s 下载完毕, %d/%d";

	@Override
	public void run() {
		File target = downloadTask.getFile();
		File parent = target.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		final HttpGet httpGet = new HttpGet(downloadTask.getUri());
		HttpAsyncRequestProducer requestProducer = HttpAsyncMethods.create(httpGet);
		ZeroCopyConsumer<File> consumer = null;
		try {
			consumer = new ZeroCopyConsumer<File>(target) {
				@Override
				protected File process(HttpResponse response, File file, ContentType contentType) throws Exception {
					return file;
				}
			};
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		File f = null;
		boolean success = false;
		while (!success) {
			Future<File> future = closeableHttpAsyncClient.execute(requestProducer, consumer, null);
			try {
				f = future.get();
				success = true;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		String msg = String.format(FORMAT, f.getName(), count.incrementAndGet(), total);
		log.info(msg);
		
	}

	/**
	 * @param httpClient
	 * @param downloadTask
	 * @param count
	 */
	public DownloaderRunnable(CloseableHttpAsyncClient closeableHttpAsyncClient, DownloadTask downloadTask,
			AtomicInteger count, int total) {
		super();
		this.closeableHttpAsyncClient = closeableHttpAsyncClient;
		this.downloadTask = downloadTask;
		this.count = count;
		this.total = total;
	}

}
