package com.quentinjian.receiptlottery;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class CheckPrize {


    public static PrizeType determinePrice(String code) {
        String[] prizeCode = MainActivity.prices;
        if (code.equals(prizeCode[0]))
            return PrizeType.SPECIAL;
        if (code.equals(prizeCode[1]))
            return PrizeType.GRAND;
        if (code.equals(prizeCode[2]) || code.equals(prizeCode[3]) || code.equals(prizeCode[4]))
            return PrizeType.FIRST;
        if (code.substring(1).equals(prizeCode[2].substring(1)) ||
            code.substring(1).equals(prizeCode[3].substring(1)) ||
            code.substring(1).equals(prizeCode[4].substring(1))) {
            return PrizeType.SECOND;
        }
        if (code.substring(2).equals(prizeCode[2].substring(2)) ||
                code.substring(2).equals(prizeCode[3].substring(2)) ||
                code.substring(2).equals(prizeCode[4].substring(2))) {
            return PrizeType.THIRD;
        }
        if (code.substring(3).equals(prizeCode[2].substring(3)) ||
                code.substring(3).equals(prizeCode[3].substring(3)) ||
                code.substring(3).equals(prizeCode[4].substring(3))) {
            return PrizeType.FOURTH;
        }
        if (code.substring(4).equals(prizeCode[2].substring(4)) ||
                code.substring(4).equals(prizeCode[3].substring(4)) ||
                code.substring(4).equals(prizeCode[4].substring(4))) {
            return PrizeType.FIFTH;
        }
        if (code.substring(5).equals(prizeCode[2].substring(5)) ||
                code.substring(5).equals(prizeCode[3].substring(5)) ||
                code.substring(5).equals(prizeCode[4].substring(5))) {
            return PrizeType.SIXTH;
        }
        return PrizeType.NONE;
    }
}
