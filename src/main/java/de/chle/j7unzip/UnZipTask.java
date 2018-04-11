package de.chle.j7unzip;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;

public class UnZipTask implements Runnable {

	private final File destinationDir;
	private File zfile;
	private KeySetView<String, Boolean> queue;
	
	private final byte[] buffer = new byte[32 * 1024 * 1024];
	private long readBytes = 0;
	private int readFiles = 0;
	
	public UnZipTask(File destinationDir, File zfile, KeySetView<String, Boolean> queue) {
		this.destinationDir = destinationDir;
		this.zfile = zfile;
		this.queue = queue;
	}
	
	private void log(String format, Object...args)
	{
		String msg = Thread.currentThread().getName() + " " + String.format(format, args);
		System.out.println(msg);
	}

	@Override
	public void run() {

		// create a new private 7zip archive and seek to the right entry
		SevenZArchiveEntry localEntry;
		try (SevenZFile sevenZfile = new SevenZFile(zfile);) {
			while ((localEntry = sevenZfile.getNextEntry()) != null) {
				
				String entryName = localEntry.getName();
				boolean entryIsTodo = queue.remove(entryName);
				if(entryIsTodo) {
					unzip(sevenZfile, localEntry);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		log("Finished %d files with %d bytes", readFiles, readBytes);
	}

	private void unzip(SevenZFile sevenZfile, SevenZArchiveEntry localEntry) {
		
		//log(localEntry.getName());

		File curfile = new File(destinationDir, localEntry.getName());
		File parent = curfile.getParentFile();
		if (!parent.exists()) {
			try {
				FileUtils.forceMkdir(parent);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		// TODO: write to file, instead of null stream

		try (NullOutputStream os = new NullOutputStream();) {
			int n = 0;
			while (-1 != (n = sevenZfile.read(buffer))) {
				os.write(buffer, 0, n);
				readBytes += n;
			}
			//log("Read bytes: %d", readBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		readFiles++;
	}

}
