package com.quentinjian.receiptlottery;

 /*
 * Some test script
 *
 * */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TestJsoupParse {
    public static void main(String[] args) throws IOException {
        boolean isNumber;
        String tax_gov_website = readStringFromURL("https://invoice.etax.nat.gov.tw/");
        Document doc = Jsoup.parse("<html><head><title>First parse</title></head>\"\n" +
                "  + \"<body><p>Parsed HTML into a doc.</p></body></html>");
        Document taxGov = Jsoup.parse(tax_gov_website);
        Elements contents = taxGov.getElementsByTag("p");
        for (Element content: contents) {
            String curContent = content.text();
            isNumber = true;
            for (int i=0;i<curContent.length();i++) {
                if ((int)curContent.charAt(i) <= (int)'9' && (int)curContent.charAt(i) >= (int)'0') {
                    continue;
                }else {
                    isNumber = false;
                    break;
                }
            }
            if (isNumber)
                System.out.println(curContent);
        }
    }

    public static String readStringFromURL(String requestURL) throws IOException
    {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
