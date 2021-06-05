package com.zheng.utils;

import com.zheng.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 郑超
 * @create 2021/6/5
 */
@Component
public class HtmlParseUtil {

    public static void main(String[] args) throws IOException {
        new HtmlParseUtil().parseJD("java").forEach(System.out::println);
    }

    public List<Object> parseJD(String keyword) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keyword + "&enc=utf-8";
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");

        List<Object> goodList = new ArrayList<>();
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            goodList.add(new Content(img, price, title));
        }

        return goodList;
    }
}
