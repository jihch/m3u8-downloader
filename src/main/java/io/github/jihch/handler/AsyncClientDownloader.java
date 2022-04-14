package io.github.jihch.handler;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jihch.model.CustomRequest;
import io.github.jihch.model.DownloadTask;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步非阻塞零拷贝下载器
 * @author jihch
 *
 */
@Component
@Slf4j
public class AsyncClientDownloader implements Downloader {

	@Autowired
	private CustomRequest customRequest;
	
	@Override
	public void download(List<DownloadTask> tasks) {
		
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(customRequest.getSocketTimeout())
				.setConnectTimeout(customRequest.getConnectTimeout()).build();
		
		SSLContext ctx = null;
		try {
			ctx = SSLContextBuilder.create().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}

		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig)
				.setSSLContext(ctx).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();

		httpclient.start();

		List<Future<File>> futureList = new LinkedList<>();

		try {

			for (DownloadTask task : tasks) {

				HttpGet httpGet = new HttpGet(task.getUri());
				
				HttpAsyncRequestProducer httpAsyncRequestProducer = HttpAsyncMethods.create(httpGet);

				File file = task.getFile();
				File parentFile = file.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				
				ZeroCopyConsumer<File> consumer = new ZeroCopyConsumer<File>(task.getFile()) {

					@Override
					protected File process(final HttpResponse response, final File file, final ContentType contentType)
							throws Exception {

						if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
							throw new ClientProtocolException("consumer process failed: " + response.getStatusLine());
						}
						return file;
					}

				};


				Future<File> future = httpclient.execute(httpAsyncRequestProducer, consumer, null);

				futureList.add(future);
			}
			
			int i = 0;
			
			String format = "%s 下载完成， %d/%d";
			
			//第一种
//			for (Future<File> future:futureList) {
//				File file = future.get();
//				i++;
//				String x = String.format(format, file.getName(), i, tasks.length);
//				log.info(x);
//			}
			
			//第二种
			while (!futureList.isEmpty()) {
				
				Iterator<Future<File>> iterator = futureList.iterator();
				
				while (iterator.hasNext()) {
					Future<File> future = iterator.next();
					if (future.isDone()) {
						File file = future.get();
						i++;
						String x = String.format(format, file.getName(), i, tasks.size());
						log.info(x);
						iterator.remove();
					}
				}
				
				TimeUnit.SECONDS.sleep(1);
				
			}
			
			//第三种
//			ExecutorService executorService = Executors.newCachedThreadPool();
//			final AtomicInteger atomicInteger = new AtomicInteger();
//			
//			for (Future<File> future : futureList) {
//				
//				executorService.execute(() -> {
//					try {
//						File file = future.get();
//						String x = String.format(format, file.getName(), atomicInteger.addAndGet(1), tasks.length);
//						log.info(x);
//					} catch (InterruptedException | ExecutionException e) {
//						e.printStackTrace();
//					}
//				});
//				
//			}
//			
//			executorService.shutdown();
//			
//	        try {
//				while (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
//				    System.out.println("线程池没有关闭");
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
