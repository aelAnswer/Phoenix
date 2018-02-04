package com.phoenix.answer.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtil {

	private final static String REF_START_PATH = "reference_prod";
	private final static String TX_START_PATH = "transactions_";
	private static final String FILE_SEPARATOR = "_";

	private FileUtil() {
	}

	public static List<Path> findAllReferenceFile(Path dir, LocalDate startDate) {
		return findAllFile(dir, reference(startDate));
	}

	public static Optional<Path> findTransactionFile(Path dir,
			LocalDate startDate) {
		return findAnyFile(dir, tx(startDate));
	}

	private static Optional<Path> findAnyFile(Path dir,
			Predicate<String> fileNameCriteria) {

		try (Stream<Path> paths = Files.find(
				dir,
				Integer.MAX_VALUE,
				(path, attrs) -> !attrs.isDirectory()
						&& fileNameCriteria.test(stripExtension(path.getFileName().toString())))) {
			return paths.findAny();
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	private static List<Path> findAllFile(Path dir,
			Predicate<String> fileNameCriteria) {

		try (Stream<Path> paths = Files
				.find(dir,
						Integer.MAX_VALUE,
						(path, attrs) -> !attrs.isDirectory()
								&& fileNameCriteria.test(stripExtension(path.getFileName().toString())))) {
			return paths.collect(Collectors.toList());
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public static void createFileWithDirectory(Path fileName) {
		try {
			if (!Files.exists(fileName.getParent())) {
				Files.createDirectory(fileName.getParent());
			}
			if (!Files.exists(fileName)) {
				Files.createFile(fileName);
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public static void write(List<String> lines, Path path) {
		try {
			Files.write(path, lines, StandardCharsets.UTF_8);
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public static Optional<String> extractShopId(Path refPath) {
		String[] name = stripExtension(refPath.getFileName().toString()).split(
				FILE_SEPARATOR);
		if (name != null && name.length == 3) {
			return Optional.of(name[1].substring(5));
		}
		return Optional.empty();
	}
	
	public static List<Path> getAllFiles(Path dir) {
		try (Stream<Path> paths = Files.walk(dir)) {
			return paths//
					.filter(Files::isRegularFile)//
					.collect(Collectors.toList());
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	private static Predicate<String> reference(LocalDate startDate) {
		return isReference().and(endWith(startDate));
	}

	private static Predicate<String> tx(LocalDate startDate) {
		return isTx().and(endWith(startDate));
	}

	private static Predicate<String> isReference() {
		return fileName -> fileName.startsWith(REF_START_PATH);
	}

	private static Predicate<String> isTx() {
		return fileName -> fileName.startsWith(TX_START_PATH);
	}

	private static Predicate<String> endWith(LocalDate startDate) {
		return fileName -> fileName.endsWith(DateAdjuster.format(startDate));
	}

	private static String stripExtension(String fullName) {
		Objects.requireNonNull(fullName);
		String fileName = new File(fullName).getName();
		int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? fullName : fileName.substring(0, dotIndex);
	}

}
