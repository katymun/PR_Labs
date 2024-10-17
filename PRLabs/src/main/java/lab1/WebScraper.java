package lab1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class WebScraper {

    public static void main(String[] args) throws Exception {
        String urlStr = "https://www.myprotein.com/c/nutrition/protein/";

        Document doc = Jsoup.connect(urlStr).get();

        Elements products = doc.select(".product-card");
        System.out.println(products.size());

        List<Product> productList = new ArrayList<>();

        for (Element product : products) {
            String name = product.select(".product-item-title").text();
            String priceStr = product.select(".price").text().replace("£", "").trim();
            String productLink = product.select("a").attr("href");
            String fullProductLink = "https://www.myprotein.com" + productLink;

            // validation
            if (name.isEmpty() || priceStr.isEmpty() || productLink.isEmpty()) {
                continue;
            }

            Document productPage = Jsoup.connect(fullProductLink).get();
            String description = productPage.select("#product-description-0").text();

            System.out.println(priceStr);
            double priceGBP = Double.parseDouble(priceStr);
            productList.add(new Product(name, priceGBP, fullProductLink, description));

//            System.out.println("Product: " + name);
//            System.out.println("Price: " + priceGBP);
//            System.out.println("Link: " + fullProductLink);
//            System.out.println("Description: " + description);
//            System.out.println("-------------------------");
        }
        double gbpToEur = 1.15;

        List<Product> convertedProducts = productList.stream()
                .map(p -> new Product(
                        p.getName(),
                        convertPrice(p.getPrice(), gbpToEur),
                        p.getLink(),
                        p.getDescription()))
                .collect(Collectors.toList());

        // filtering the products
        List<Product> filteredProducts = convertedProducts.stream()
                .filter(p -> p.getPrice() >= 10 && p.getPrice() <= 30)
                .collect(Collectors.toList());

        System.out.println("Filtered products:");
        for (Product prod : filteredProducts) {
            System.out.println(prod);
        }

        double totalSum = filteredProducts.stream()
                .map(Product::getPrice)
                .reduce(0.0, Double::sum);

        System.out.println("Total Price of Filtered Products: " + totalSum);
        System.out.println("Timestamp (UTC): " + Instant.now());
    }

    private static double convertPrice(double priceGBP, double gbpToEur) {
        return priceGBP * gbpToEur;
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
