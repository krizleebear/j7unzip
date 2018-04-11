package de.chle.j7unzip;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

public class J7Unzip {

	static String dir = "/Users/krizleebear/Downloads";

	private final File zipFile;
	private final File destinationDir;

	public J7Unzip(File zipFile, File destinationDir) {
		this.zipFile = zipFile;
		this.destinationDir = destinationDir;
	}

	public static void main(String... args)
			throws FileNotFoundException, IOException, ArchiveException, InterruptedException {
		File folder = new File(dir);
		File swupZip = findSWUPFile(folder);
		System.out.println(swupZip);

		File destinationDir = new File("./unzipped");

		long startMillis = System.currentTimeMillis();
		
		int availableCPUCores = Runtime.getRuntime().availableProcessors();
		int threadCount = availableCPUCores;

		J7Unzip unzipper = new J7Unzip(swupZip, destinationDir);
		unzipper.unzip(threadCount);

		long durationMillis = System.currentTimeMillis() - startMillis;

		System.out.println("Duration (seconds): " + durationMillis / 1000);
	}

	private void unzip(int threadCount) throws IOException, InterruptedException {

		KeySetView<String, Boolean> tasks = ConcurrentHashMap.newKeySet();

		SevenZArchiveEntry entry;
		try (SevenZFile sevenZfile = new SevenZFile(zipFile);) {

			while ((entry = sevenZfile.getNextEntry()) != null) {

				if (entry.isDirectory()) {
					continue;
				} else {
					tasks.add(entry.getName());
				}
			}
		}
		
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		for (int i = 0; i < threadCount; i++) {
			executorService.execute(new UnZipTask(destinationDir, zipFile, tasks));
		}
		
		ProgressTask progress = new ProgressTask(tasks);
		Thread t = new Thread(progress);
		t.start();

		executorService.shutdown();
		executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.HOURS);
		System.out.println("finished");
	}

	private static File findSWUPFile(File folder) {
		File[] files = folder.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return (pathname.getName().endsWith("_SWUP.7z"));
			}
		});
		if (files == null || files.length != 1) {
			throw new RuntimeException("SWUP file not found in folder " + folder.getAbsolutePath());
		}
		return files[0];
	}

}
