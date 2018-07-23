package de.chle.j7unzip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 * Creates a mock version of the given ZIP file. <br/>
 * The mock will contain the same structure and files, but all files with empty
 * content.
 */
public class ZipMock
{
	public static class FakeEntry implements ArchiveEntry
	{
		private final String name;
		private final boolean isDirectory;
		private final Date lastModified;

		public FakeEntry(ArchiveEntry original)
		{
			name = original.getName();
			isDirectory = original.isDirectory();
			lastModified = original.getLastModifiedDate();
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public long getSize()
		{
			return 0;
		}

		@Override
		public boolean isDirectory()
		{
			return isDirectory;
		}

		@Override
		public Date getLastModifiedDate()
		{
			return lastModified;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	public static void mock(File originalFile, File destFile)
			throws FileNotFoundException, IOException, ArchiveException
	{
		List<ArchiveEntry> entries = readEntries(originalFile);
		System.out.println(entries);
		writeEntries(destFile, entries);
	}

	private static List<ArchiveEntry> readEntries(File originalFile)
			throws FileNotFoundException, IOException, ArchiveException
	{
		try (FileInputStream fin = new FileInputStream(originalFile);
				BufferedInputStream bin = new BufferedInputStream(fin);
				ArchiveInputStream input = new ArchiveStreamFactory()
						.createArchiveInputStream(bin);)
		{
			List<ArchiveEntry> fakeEntries = new ArrayList<>();
			ArchiveEntry original = null;
			while ((original = input.getNextEntry()) != null)
			{
				fakeEntries.add(new FakeEntry(original));
			}

			return fakeEntries;
		}
	}

	private static void writeEntries(File destFile, List<ArchiveEntry> entries)
			throws IOException
	{
		try (ArchiveOutputStream o = new ZipArchiveOutputStream(destFile))
		{
			for (ArchiveEntry originalEntry : entries)
			{
				ZipArchiveEntry fakeEntry = new ZipArchiveEntry(
						originalEntry.getName());

				FileTime lastModified = FileTime.fromMillis(
						originalEntry.getLastModifiedDate().getTime());
				fakeEntry.setLastModifiedTime(lastModified);

				// potentially add more flags to entry
				o.putArchiveEntry(fakeEntry);
				o.closeArchiveEntry();
			}
			o.finish();
		}
	}
}
