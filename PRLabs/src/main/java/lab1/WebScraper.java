package lab1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class WebScraper {

    public static void main(String[] args) throws Exception {
        String urlStr = "https://www.myprotein.com/c/nutrition/protein/";

        String htmlContent = fetchPage(urlStr);

        Document doc = Jsoup.parse(htmlContent);

        Elements products = doc.select(".product-card");

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

//            System.out.println(priceStr);
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

    private static String fetchPage(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        String host = url.getHost();
        String path = url.getPath();

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, 443)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            writer.write("GET " + path + " HTTP/1.1\r\n");
            writer.write("Host: " + host + "\r\n");
            writer.write("Connection: close\r\n");
            writer.write("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3\r\n");
            writer.write("\r\n");
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            boolean isBody = false;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isBody = true;
                } else if (isBody) {
                    response.append(line).append("\n");
                }
            }
            System.out.println("response: " + response.toString());
            return response.toString();
        }
    }

    private static double convertPrice(double priceGBP, double gbpToEur) {
        return priceGBP * gbpToEur;
    }
}
