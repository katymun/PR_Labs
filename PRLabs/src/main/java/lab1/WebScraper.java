package lab1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class WebScraper {
    public static void main(String[] args) throws Exception {
        String urlStr = "https://www.myprotein.com/c/nutrition/protein/";

//        String htmlContent = fetchPage(urlStr);
//        System.out.println(htmlContent);
        Document doc = Jsoup.connect(urlStr).get();

        Elements products = doc.select(".product-card");
        System.out.println(products.size());
        for (Element product : products) {
            String name = product.select(".product-item-title").text();
            String price = product.select(".price").text();
            String productLink = product.select("a").attr("href");
            String fullProductLink = "https://www.myprotein.com" + productLink;
            String imageUrl = product.select("img").attr("src");

            System.out.println("Product: " + name);
            System.out.println("Price: " + price);
            System.out.println("Link: " + fullProductLink);
            System.out.println("Image URL: " + imageUrl);
            System.out.println("-------------------------");
        }
    }

    private static String fetchPage(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        if (con.getResponseCode() >= 200 && con.getResponseCode() < 300) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            return content.toString();
        } else {
            throw new Exception("Failed to fetch the page. Status code: " + con.getResponseCode());
        }
    }
}
