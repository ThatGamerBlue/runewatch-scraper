package com.thatgamerblue.oprs.runewatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser
{
	private static URL BASE_URL;
	private static String ALPHABET = "abcdefghijklmnopqrstuvwxyz1234567890";
	private static String xsrfToken = "";
	private static String sessionToken = null;

	static
	{
		try
		{
			BASE_URL = new URL("https://runewatch.com/cases/list/");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException
	{
		/*String startingChar = args.length == 1 ? "a" : args[1];

		Set<String> finalNames = new HashSet<>();

		System.out.println("Obtaining cookies...");
		makeRequest(BASE_URL);

		System.out.println("Got session: " + sessionToken + " and xsrf: " + xsrfToken);

		for (int i = ALPHABET.indexOf(startingChar); i < ALPHABET.length(); i++)
		{
			String str = String.valueOf(ALPHABET.charAt(i));
			URL request = new URL(BASE_URL, str);
			String toParse = makeRequest(request);
			toParse = toParse.replace("\r", "");
			for (String line : toParse.split("\n"))
			{
				line = line.trim();
				if (line.startsWith("<a href"))
				{
					System.out.println(line);
					String name = line.substring("<a href=\"/cases/".length());
					name = name.substring(0, name.length() - 2);
					finalNames.add(name);
				}
			}
			Thread.sleep(100);
		}*/
		Set<String> finalNames = new HashSet<>();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		URL apiUrl = new URL("https://www.runewatch.com/api/cases/list");
		System.out.println("Obtaining cookies...");
		makeRequest(BASE_URL);

		List<RunewatchCase> caseList = gson.fromJson(makeRequest(apiUrl), new TypeToken<List<RunewatchCase>>(){}.getType());

		caseList.forEach(c -> finalNames.add(c.accused_rsn));

		String json = gson.toJson(finalNames);
		System.out.println(json);
		File outFile = new File(args[0]);
		if (outFile.exists())
		{
			outFile.delete();
		}
		Files.write(outFile.toPath(), json.getBytes(), StandardOpenOption.CREATE_NEW);
	}

	private static String makeRequest(URL url) throws IOException
	{
		System.out.println("Requesting: " + url.toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		if (sessionToken != null)
		{
			connection.setRequestProperty("Cookie", xsrfToken + "; " + sessionToken + "; core=valid");
		}
		if (connection.getResponseCode() != 200)
		{
			// n returns a consistent 500 error, even accessing it "properly"
			if (url.toString().charAt(url.toString().length() - 1) == 'n' && connection.getResponseCode() == 500)
			{
				return "";
			}
			throw new RuntimeException(
				"we got blocked from runewatch, shit lmao, response code " + connection.getResponseCode());
		}

		List<String> cookieHeaders = connection.getHeaderFields().get("Set-Cookie");

		for (String s : cookieHeaders)
		{
			if (s.startsWith("runewatch_session"))
			{
				sessionToken = s.split(";")[0];
			}
			else if (s.startsWith("XSRF_TOKEN"))
			{
				xsrfToken = s.split(";")[0];
			}
		}

		StringBuffer sb = new StringBuffer();
		try (InputStream is = connection.getInputStream())
		{
			int i;
			while ((i = is.read()) != -1)
			{
				sb.append((char) i);
			}
		}
		return sb.toString();
	}
}
