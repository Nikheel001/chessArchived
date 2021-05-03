package com.headshot.chessarchived;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author nikheel.patel
 *
 */
public class ChessArchivedUtility {

	private static String PGN = "pgn";
	private static String SEP = "/";
	private static String gameapiURL;

	/**
	 * based on <i> userName </i>, <i> baseUrl </i> and <i> endpoint </i> fetches
	 * url for avalible games groupes them by month and adds to <i> urls </i>
	 */
	public String getAvalibleGamesGroupedByMonth(String userName, String baseUrl, String endpoint, Set<URI> urls) {
		String prefix = "";
		try {
			HttpRequest req = HttpRequest.newBuilder(new URI(baseUrl + userName + endpoint))
					.version(HttpClient.Version.HTTP_2).GET().build();

			HttpResponse<String> response = HttpClient.newBuilder().build().send(req, BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				String x = response.body();
				if (x != null) {
					HashMap<String, List<String>> tmp = new ObjectMapper().readValue(x, HashMap.class);

					for (String i : tmp.getOrDefault("archives", List.of())) {
						if (gameapiURL == null) {
							gameapiURL = i;
						}
						urls.add(new URI(i + SEP + PGN));
					}
					if (gameapiURL != null) {
						prefix = gameapiURL.substring(0, gameapiURL.length() - 7);
					}

					// removing current month's games
					if (YearMonth.now().getMonthValue() > 9) {
						System.out.println(
								prefix + YearMonth.now().getYear() + SEP + YearMonth.now().getMonthValue() + SEP + PGN);
						if (urls.remove(new URI(prefix + YearMonth.now().getYear() + SEP
								+ YearMonth.now().getMonthValue() + SEP + PGN))) {
							System.out.println(
									"ignoring current month's games \\n****you can download manually from above mentioned url****");
						}
					} else {

						System.out.println(prefix + YearMonth.now().getYear() + SEP + "0"
								+ YearMonth.now().getMonthValue() + SEP + PGN);

						if (urls.remove(new URI(prefix + YearMonth.now().getYear() + SEP + "0"
								+ YearMonth.now().getMonthValue() + SEP + PGN))) {
							System.out.println(
									"ignoring current month's games \n****you can download manually from above mentioned url****");
						}
					}

				} else {
					System.err.println("fetching AvalibleGamesGroupedByMonth failed");
				}
			} else {
				System.err.println("fetching AvalibleGamesGroupedByMonth failed check username or api endpoints");
			}

		} catch (URISyntaxException e) {
			System.err.println(e.getMessage());
			System.out.println("check if url is invalid");
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println("check for permissions");
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.out.println("Could not finish http request");
		}
		return prefix;
	}

	/**
	 * stores pgn to <i> path </i> and ignores already stored games
	 * 
	 * @param path
	 * @param urlPrefix
	 * @param urls
	 */
	public void saveGamesToDir(String path, String urlPrefix, Set<URI> urls) {

		File d = new File(path);
		if (d.isDirectory() && urlPrefix.length() > 0) {
			try {
				for (File fp : d.listFiles()) {
					String nm = fp.getName();
					if (fp.isFile() && nm.endsWith(".pgn")) {
						StringBuilder sb = new StringBuilder();
						sb.append(urlPrefix).append(nm.substring(0, 4)).append(SEP).append(nm.substring(5, 7))
								.append(SEP).append(PGN);
						System.out.println("Already Present : " + sb.toString());
						urls.remove(new URI(sb.toString()));
					}
				}
			} catch (URISyntaxException e) {
				System.err.println("unable to remove already processed requests");
			}

			System.out.println("======================================");
			System.out.println("Downloading ...");
			System.out.println("======================================");

			urls.forEach(System.out::println);

			HttpClient client = HttpClient.newHttpClient();
			List<HttpRequest> requests = urls.stream().map(HttpRequest::newBuilder)
					.map(reqBuilder -> reqBuilder.build()).collect(Collectors.toList());
			CompletableFuture.allOf(requests.stream().map(request -> {
				String url = request.uri().toString();
				url = url.substring(url.length() - 11, url.length() - 4);
				url = path + "/" + url.replace('/', '_') + ".pgn";
				return client.sendAsync(request, BodyHandlers.ofFile(Paths.get(url)));
			}).toArray(CompletableFuture<?>[]::new)).join();
		} else if (!d.isDirectory()) {
			System.err.println("invalid directory");
		} else {
			System.err.println("no games found");
		}

	}

//	public boolean isValidUser(String useName, String baseUrl) {
//		try {
//			HttpClient client = HttpClient.newHttpClient();
//			HttpRequest req = HttpRequest.newBuilder(new URI(baseUrl + useName)).version(HttpClient.Version.HTTP_2)
//					.build();
//
//			HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
//
//			if (res.statusCode() != 200) {
//				return false;
//			} else {
//				return true;
//			}
//
//		} catch (URISyntaxException e) {
//			System.err.println(e.getMessage());
//			System.out.println("check if url is invalid");
//		} catch (IOException e) {
//			System.err.println(e.getMessage());
//			System.out.println("check for permissions");
//		} catch (InterruptedException e) {
//			System.err.println(e.getMessage());
//			System.out.println("Could not finish http request");
//		}
//		return false;
//	}
}
