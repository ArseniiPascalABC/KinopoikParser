import com.opencsv.CSVWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class KinopoiskScraper {

    public static void main(String[] args) throws InterruptedException {

        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver-win64/chromedriver-win64/chromedriver.exe");

        WebDriver driver = new ChromeDriver();

        String url = "https://www.kinopoisk.ru/film/326/";
        String csvFilePath = "reviews.csv";

        try {
            driver.get(url);
            Thread.sleep(30000);

            long pageHeight = (long) ((JavascriptExecutor) driver).executeScript("return Math.max( document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);");

            scrollPage((JavascriptExecutor) driver, pageHeight);
            scrollPage((JavascriptExecutor) driver, pageHeight);

            String pageSource = driver.getPageSource();

            Document document = Jsoup.parse(pageSource);

            Element reviewsContainer = document.selectFirst(".styles_column__r2MWX.styles_md_12__qg1CD.styles_lg_16__0uYDp");
            assert reviewsContainer != null;

            Elements reviewSections = reviewsContainer.select("section");

            try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {
                for (Element reviewSection : reviewSections) {
                    String colorClass = Objects.requireNonNull(reviewSection.selectFirst(".styles_review__GN2Uy")).className();
                    String color = colorClass.contains("styles_rootPositive") ? "Зеленый" : "Красный";

                    String author = Objects.requireNonNull(reviewSection.selectFirst(".styles_nameLink__l1phW")).text();
                    writer.writeNext(new String[]{"Автор: " + author});

                    String title = Objects.requireNonNull(reviewSection.selectFirst(".styles_title__utMMO")).text();
                    writer.writeNext(new String[]{"Цвет: " + color});
                    writer.writeNext(new String[]{"Заголовок: " + title});

                    String text = Objects.requireNonNull(reviewSection.selectFirst(".styles_text__AYoL6 span")).text();
                    writer.writeNext(new String[]{"Текст: " + text});

                    writer.writeNext(new String[]{""});
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } finally {
            driver.quit();
        }
    }

    private static void scrollPage(JavascriptExecutor driver, long pageHeight) throws InterruptedException {
        driver.executeScript("window.scrollBy(0, " + (pageHeight) + ")");
        Thread.sleep(1000);
        driver.executeScript("window.scrollBy(0, " + (pageHeight) + ")");
        Thread.sleep(1000);
    }
}
