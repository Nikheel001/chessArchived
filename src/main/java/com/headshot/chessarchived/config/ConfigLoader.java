package com.headshot.chessarchived.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.headshot.chessarchived.ChessarchivedMain;

/**
 * 
 * @author nikheel.patel
 *
 */
public class ConfigLoader {

	private static Properties config;

	public static Object getOrDefault(Object key, Object defaultValue) {
		return config.getOrDefault(key, defaultValue);
	}

	// loads configurations from property file
	public static void init() {
		config = new Properties();
		String propFilePath = System.getProperty("config");

		if (propFilePath != null) {

			try (InputStream input = new FileInputStream(new File(propFilePath))) {
				if (input != null) {
					config.load(input);
				}
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
				System.out.println(" Propertyfile path specified is incorrect : " + propFilePath);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.out.println("check for permissions");
			}

		} else {
			try (InputStream input = ChessarchivedMain.class.getClassLoader()
					.getResourceAsStream("application.properties")) {

				if (input != null) {
					config.load(input);
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.out.println("jar is currupted");
			}
		}
	}

}
