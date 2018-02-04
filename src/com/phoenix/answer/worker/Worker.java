package com.phoenix.answer.worker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.phoenix.answer.io.ModelWriter;
import com.phoenix.answer.model.Model;
import com.phoenix.answer.util.DateAdjuster;
import com.phoenix.answer.util.FileUtil;

/**
 * 
 * @author ael
 *
 */
public class Worker {

	private final Path dir;
	private final ModelWriter modelHandler;

	public Worker(Path dir) {
		super();
		this.dir = dir;
		this.modelHandler = new ModelWriter(dir);
	}

	public void findAll(LocalDate startDate, int nbDays, int k) {
		Instant start = Instant.now();
		FileUtil.getAllFiles(Paths.get(dir.toString(), DateAdjuster.format(startDate)));
		//TODO		
		Instant end = Instant.now();
		System.out.println("worker : " + getTiming(start, end) + " seconds");
	}
	
	/**
	 * Orchestrate the work: start from date, determine the last nbDays, and then calculate top topk CA and sales
	 * for each day, store the result into adequate file
	 * @param startDate
	 * @param nbDays
	 * @param topK
	 */
	public void executeAll(LocalDate startDate, int nbDays, int topK) {
		Instant start = Instant.now();
		List<LocalDate> days = DateAdjuster.getLastKPreviousDays(startDate,
				nbDays);
		days.forEach(day -> executeAllTaskForDay(day, topK));
		Instant end = Instant.now();
		System.out.println("worker : " + getTiming(start, end) + " seconds");
	}

	/**
	 * find path of resources (ref and transaction), then calculate top topk sales and CA for startDate
	 * @param startDate
	 * @param topK
	 */
	public void executeAllTaskForDay(LocalDate startDate, int topK) {
		Instant start = Instant.now();
		List<Path> refPaths = FileUtil.findAllReferenceFile(dir, startDate);
		Optional<Path> txPath = FileUtil.findTransactionFile(dir, startDate);

		if (refPaths.isEmpty() || !txPath.isPresent()) {
			System.out.println("No task for this date: " + startDate);
		} else {
			refPaths.stream().forEach(
					(refPath) -> executeTask(startDate, txPath.get(), refPath,topK));
			Instant end = Instant.now();
			System.out.println("worker for date : " + startDate + " take: "
					+ getTiming(start, end) + " seconds");
		}
	}

	/**
	 * calculate top topk sales and CA for startDate
	 * @param startDate
	 * @param txPath transaction path
	 * @param refPath referencial path
	 * @param topK
	 */
	public void executeTask(LocalDate startDate, Path txPath, Path refPath,
			int topK) {
		Instant start = Instant.now();

		Optional<String> oShopId = FileUtil.extractShopId(refPath);
		if (oShopId.isPresent()) {
			//use only 2 thread (quiz constraints)
			ExecutorService executor = Executors.newFixedThreadPool(2);
			try {
				String shopId = oShopId.get();

				CompletableFuture<Map<String, Double>> refDatas = CompletableFuture
						.supplyAsync(() -> modelHandler.getDataFromReferential(refPath),
								executor);

				CompletableFuture<Map<String, Model>> txDatas = CompletableFuture
						.supplyAsync(
								() -> modelHandler.getDataFromTransaction(txPath, shopId),
								executor);

				refDatas.thenCombine(txDatas, this::merge)//
						.thenApply(Map::values)//
						.thenAccept(
								(models) -> modelHandler.write(startDate, topK, shopId, models));

				refDatas.join();
			} finally {
				executor.shutdown();
			}
		}

		Instant end = Instant.now();
		System.out.println("Time taken: " + getTiming(start, end) + " seconds");
	}

	private static double getTiming(Instant start, Instant end) {
		return Duration.between(start, end).toMillis() / 1000.0;
	}
	
	//merge refDatas int transaction datas, put price into model
	private Map<String, Model> merge(Map<String, Double> refDatas, Map<String, Model> txDatas) {
		for (Entry<String, Model> entry : txDatas.entrySet()) {
			Double data = refDatas.get(entry.getKey());
			if (data != null) {
				entry.getValue().setUnitPrice(data);
			}
		}
		return txDatas;
	}
	
}
