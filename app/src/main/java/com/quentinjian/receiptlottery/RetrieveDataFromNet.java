package com.quentinjian.receiptlottery;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RetrieveDataFromNet {

    public static String[] retrieveCode(String sourceUrl) throws IOException {
        boolean isNumber;
        String[] result = new String[10];
        int numCount = 0;
        String tax_gov_website = readStringFromURL(sourceUrl);
        Document taxGov = Jsoup.parse(tax_gov_website);
        Elements contents = taxGov.getElementsByTag("p");
        for (Element content: contents) {
            String curContent = content.text();
            isNumber = true;
            for (int i=0;i<curContent.length();i++) {
                if (!((int)curContent.charAt(i) <= (int)'9') &&
                        !((int)curContent.charAt(i) >= (int)'0')) {
                    isNumber = false;
                    break;
                }
            }
            if (numCount > 5) //We only need 5 number from the government website
                break;
            if (isNumber)
                result[numCount++] = curContent;
        }
        return result;
    }

    private static String readStringFromURL(String requestURL) throws IOException
    {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

}
