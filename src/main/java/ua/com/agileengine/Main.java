package ua.com.agileengine;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Main {

	private static Logger LOG = Logger.getLogger(Main.class);

	private static String CHARSET_NAME = "utf8";

	public static void main(String[] args) {
		if (areInputParamsValid(args)) {
			Element targetElement = loadTargetElement(args[0]);

			Element resultElement = findElementById(new File(args[1]), targetElement.id()).orElse(null);

			//It was not found by ID
			if (resultElement == null) {
				//Find by class
				Elements elementsByQuery = findElementsByQuery(new File(args[1]), "a[class*=\"" + targetElement.className() + "\"]").orElseGet(Elements::new);

				//Smth was found by class
				if (!elementsByQuery.isEmpty()) {
					//Specify element with inside text as there is more then 1 element
					if (elementsByQuery.size() > 1) {
						resultElement = findElementsByQuery(new File(args[1]),
								"a[class*=\"" + targetElement.className() + "\"]" +
										":contains(" + targetElement.text() + ")").orElseGet(Elements::new).first();
					} else {
						resultElement = elementsByQuery.first();
					}
				} else {
					//Nothing was found by class
					//Find by text
					elementsByQuery = findElementsByQuery(new File(args[1]), "a:contains(" + targetElement.text().trim() + ")").orElseGet(Elements::new);

					//Specify element with href as there is more then 1 element
					if (elementsByQuery.size() > 1) {
						String elementHref = targetElement.attr("href");
						resultElement = findElementsByQuery(new File(args[1]),
								"a[href*=" + elementHref.substring(elementHref.lastIndexOf("#") + 1) + "]" +
										":contains(" + targetElement.text() + ")").orElseGet(Elements::new).first();
					} else {
						resultElement = elementsByQuery.first();
					}

				}
			}

			LOG.info(getElementDOMPath(resultElement));
		} else {
			LOG.error("Input params are not valid");
		}

	}

	private static boolean areInputParamsValid(String[] args) {
		boolean result = false;
		if (args != null && args.length >= 2) {
			if (args[0] != null && !args[0].isEmpty()) {
				if (args[1] != null && !args[1].isEmpty()) {
					result = true;
				}
			}
		}

		return result;
	}

	private static Element loadTargetElement(String targetFilePath) {
		String targetElementId = "make-everything-ok-button";

		Optional<Element> element = findElementById(new File(targetFilePath), targetElementId);

		return element.orElseGet(() -> new Element(""));
	}

	private static Optional<Element> findElementById(File htmlFile, String targetElementId) {
		try {
			Document doc = Jsoup.parse(
					htmlFile,
					CHARSET_NAME,
					htmlFile.getAbsolutePath());

			return Optional.ofNullable(doc.getElementById(targetElementId));

		} catch (IOException e) {
			LOG.error("Error reading " + htmlFile.getAbsolutePath() + " file", e);
			return Optional.empty();
		}
	}

	private static Optional<Elements> findElementsByQuery(File htmlFile, String cssQuery) {
		try {
			Document doc = Jsoup.parse(
					htmlFile,
					CHARSET_NAME,
					htmlFile.getAbsolutePath());

			return Optional.ofNullable(doc.select(cssQuery));
		} catch (IOException e) {
			LOG.error("Error reading " + htmlFile.getAbsolutePath() + " file", e);
			return Optional.empty();
		}
	}

	private static String getElementDOMPath(Element element) {
		StringBuilder result = new StringBuilder();

		element.parents().forEach(parent -> result.insert(0, parent.tagName() + " -> "));
		result.append(element.tagName());

		return result.toString();
	}
}
