package com.headshot.chessarchived;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.headshot.chessarchived.config.ConfigLoader;

/**
 * 
 * @author nikheel.patel
 *
 */
public class ChessarchivedMain {

	public static void main(String[] args) {
		System.out.println("Welcome headshot");
		ConfigLoader.init();

		String baseurl = (String) ConfigLoader.getOrDefault("baseurl", "");
		String userName = (String) ConfigLoader.getOrDefault("username", "");
		String archiveEndPoint = (String) ConfigLoader.getOrDefault("archiveEndPoint", "");
		String dir = (String) ConfigLoader.getOrDefault("dir", "");

		Set<URI> uris = new HashSet<>();

		ChessArchivedUtility ul = new ChessArchivedUtility();
//		if(ul.isValidUser(userName, baseurl)) {			
		String pre = ul.getAvalibleGamesGroupedByMonth(userName, baseurl, archiveEndPoint, uris);
		ul.saveGamesToDir(dir, pre, uris);
//		}else {
//			System.err.println("invalid username");
//		}
	}
}