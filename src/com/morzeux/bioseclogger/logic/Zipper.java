package com.morzeux.bioseclogger.logic;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import android.os.Environment;

/**
 * Zipper class controls packing and unpacking zip files.
 * 
 * @author Stefan Smihla
 * 
 */
public enum Zipper {
	INSTANCE;

	/**
	 * Returns actual date as string. This is mostly in generating file name.
	 * 
	 * @return actual date as string
	 */
	public static String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.ENGLISH);
		return dateFormat.format(new Date());
	}

	/**
	 * Returns absolute path to file.
	 * 
	 * @param fileName
	 *            name of file
	 * @return absolute path to file
	 */
	public static String getExternalPath(String fileName) {
		return Environment.getExternalStorageDirectory().toString() + "/"
				+ fileName;
	}

	/**
	 * Deletes specific file or directory with files recursively.
	 * 
	 * @param file
	 *            file or directory name
	 */
	private static void deleteFile(File file) {
		if (file.isDirectory())
			for (File subFile : file.listFiles())
				deleteFile(subFile);

		file.delete();
	}

	/**
	 * Delete specific file or directory.
	 * 
	 * @param fileName
	 *            file or directory name
	 */
	public static void deleteFile(String fileName) {
		deleteFile(new File(getExternalPath(fileName)));
	}

	/**
	 * Checks if unpacked directory exists and deletes if yes.
	 * 
	 * @param zFile
	 *            packed zip file to be extracted
	 * @throws ZipException
	 *             raises on damaged zip file
	 */
	private static void deleteFilesBeforeExtraction(ZipFile zFile)
			throws ZipException {
		@SuppressWarnings("unchecked")
		List<FileHeader> files = zFile.getFileHeaders();
		for (FileHeader file : files) {
			deleteFile(file.getFileName().split("/")[0]);
		}
	}

	/**
	 * Extracts zip file.
	 * 
	 * @param zipPath
	 *            input zip file
	 * @param extractPath
	 *            output file path
	 * @return output file path
	 * @throws ZipException
	 *             raises on damaged zip file
	 */
	public static String unzip(String zipPath, String extractPath)
			throws ZipException {
		ZipFile zFile = new ZipFile(new File(getExternalPath(zipPath)));
		deleteFilesBeforeExtraction(zFile);

		zFile.extractAll(getExternalPath(extractPath));
		return extractPath;
	}

	/**
	 * Packs folder or file into zip file.
	 * 
	 * @param folderPath
	 *            folder to zip
	 * @param outputZip
	 *            output file name
	 * @return output file name
	 * @throws ZipException
	 *             raises on damaged zip file
	 */
	public static String zip(String folderPath, String outputZip)
			throws ZipException {
		ZipFile zFile = new ZipFile(getExternalPath(outputZip));
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

		zFile.createZipFileFromFolder(getExternalPath(folderPath), parameters,
				false, 0);
		return outputZip;
	}
}
