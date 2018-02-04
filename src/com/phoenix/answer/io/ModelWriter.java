package com.phoenix.answer.io;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.phoenix.answer.model.Model;
import com.phoenix.answer.util.DataParser;
import com.phoenix.answer.util.DateAdjuster;
import com.phoenix.answer.util.FileUtil;
import com.phoenix.answer.util.ModelUtil;

public class ModelWriter {

	private final static String LINE_SEPARATOR = "\\|";
	private final static String TOP_100 = "top_100";
	private final static String TOP_100_SALES = TOP_100 + "_Ventes_";
	private final static String TOP_100_CA = TOP_100 + "_ca_";
	private final static String EXT_PATH = ".data";
	private static final String FILE_SEPARATOR = "_";
	private final Path dir;

	public ModelWriter(Path dir) {
		super();
		this.dir = dir;
	}
	
	public void write(LocalDate startDate, int k, String shopId,
			Collection<Model> models) {
		List<Model> topCa = ModelUtil.findKthUsingHeap(models,
				Comparator.comparing(Model::getCa), byCa(), k);
		write(topCa, shopId, startDate, TOP_100_CA, k);
		List<Model> topSales = ModelUtil.findKthUsingHeap(models,
				Comparator.comparing(Model::getQuantity), bySales(), k);
		write(topSales, shopId, startDate, TOP_100_SALES, k);
	}

	private void write(List<Model> topCa, String shopId, LocalDate startDate,
			String prefix, int k) {
		Path topCaPath = getPath(startDate, prefix, shopId);
		FileUtil.createFileWithDirectory(topCaPath);
		write(topCaPath, topCa);
	}

	public Map<String, Double> getDataFromReferential(Path refPath) {
		Optional<String> oShopId = FileUtil.extractShopId(refPath);
		if (oShopId.isPresent()) {
			try (Stream<String> lines = Files.lines(refPath)) {
				return lines//
						.map(line -> line.split(LINE_SEPARATOR))//
						.collect(
								Collectors.toMap(e -> e[0],
										e -> DataParser.parseDouble(e[1])));
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
		return Collections.<String, Double> emptyMap();
	}

	/**
	 * extract datas from transaction file, if quantity is put 0
	 * @param txPath
	 * @param shopId
	 * @return
	 */
	public Map<String, Model> getDataFromTransaction(Path txPath, String shopId) {
		Map<String, Model> counter = new ConcurrentHashMap<>();

		try (Stream<String> lines = Files.lines(txPath)) {
			lines//
			.filter(line -> line.contains(shopId))//
					.map(line -> line.split(LINE_SEPARATOR))//
					.forEach(
							line -> counter.computeIfAbsent(line[3],
									t -> new Model(line[3])).addQuantity(
									DataParser.parseInteger(line[4])));
			return counter;
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	private static BiFunction<Model, Model, Boolean> bySales() {
		return (m1, m2) -> m1.getQuantity() > m2.getQuantity();
	}

	private static BiFunction<Model, Model, Boolean> byCa() {
		return (m1, m2) -> m1.getCa() > m2.getCa();
	}

	private String getStringFromModel(Model model) {
		return model.getProductId() + "|" + model.getQuantity() + "|"
				+ model.getCa();
	}

	private void write(Path path, List<Model> models) {
		List<String> lines = models.stream()//
				.map(this::getStringFromModel)//
				.collect(Collectors.toCollection(() -> new ArrayList<>(models.size())));
		FileUtil.write(lines, path);
	}

	private Path getPath(LocalDate startDate, String type, String shopId) {
		String date = DateAdjuster.format(startDate);
		return Paths.get(dir.toString(), date + File.separator + type + shopId
				+ FILE_SEPARATOR + date + EXT_PATH);
	}
}
