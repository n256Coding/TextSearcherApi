package com.n256coding.Dev;

import it.unimi.dsi.fastutil.Hash;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

public class JSoupSessionTester {
    public void viewSite() throws IOException {
        //Secure Page
        //https://www.safaribooksonline.com/library/view/java-language-features/9781484233481/A323070_2_En_2_Chapter.html
        //csrfsafari=iKVftV8nkEvU4nUaPcSDW7KHFzikensM;
        // BrowserCookie=4a1b25e3-7870-4848-a498-bcd04acea363;
        // corp_sessionid=p37x9dkmc5oczgf33bb8xclskdqa36uk;
        // _ga=GA1.2.1167325580.1529093162;
        // _gid=GA1.2.227569139.1529093162;
        // _vwo_uuid_v2=D758DD8172138E6D4A1202C95F2D997D1|82db3a12aece6f72f480e9cbd880e7ba;
        // kampyle_userid=d5e3-50bb-b92f-f3de-1044-be80-8d49-00d6;
        // kampyleUserSession=1529141579299;
        // kampyleSessionPageCounter=2;
        // kampyleUserSessionsCount=6;
        // cd_user_id=164050e0fec10e-095c3b9feb372e-4c312a7a-100200-164050e0fee191;
        // salesforce_id=203789876130cb1c8557a20fffc72b23;
        // recently-viewed=%5B%220596007124%22%2C%229781484233481%3AA323070_2_En_2_Chapter.html%22%2C%221565924185%22%5D;
        // original_referer="https://www.google.com/";
        // timezoneoffset=-19800;
        // liveagent_oref=https://www.safaribooksonline.com/;
        // liveagent_vc=1;
        // sessionid=5qdqj9wh3t7e82lxzot3byeg3wgwgba4;
        // optimizelyEndUserId=oeu1529141576092r0.4152480509173647;
        // optimizelySegments=%7B%22757067938%22%3A%22direct%22%2C%22778703350%22%3A%22false%22%2C%22781081607%22%3A%22ff%22%2C%22949601412%22%3A%22none%22%7D;
        // optimizelyBuckets=%7B%7D;
        // _gat=1;
        // _uetsid=_uet18e50cb2;
        // _gali=id_email
        //csrfmiddlewaretoken


        String sessionID = "mjzill1s760pbcrjot9g5pmttahefxec";
        String username = "pijag@fxprix.com";
        String password = "abc#321";
        final String USER_AGENT = "\"Mozilla/5.0 (Windows NT\" +\n" +
                "          \" 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2\"";

        HashMap<String, String> headers = new HashMap<>();
        HashMap<String, String> cookies = new HashMap<>();
        headers.put("Host", "www.safaribooksonline.com");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Language", "en,si;q=0.8,fr;q=0.6,ja;q=0.4,chrome://global/locale/intl.properties;q=0.2");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Referer", "https://www.safaribooksonline.com/");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Content-Length", "174");
        headers.put("Cookie", "csrfsafari=iKVftV8nkEvU4nUaPcSDW7KHFzikensM; BrowserCookie=4a1b25e3-7870-4848-a498-bcd04acea363; corp_sessionid=p37x9dkmc5oczgf33bb8xclskdqa36uk; _ga=GA1.2.1167325580.1529093162; _gid=GA1.2.227569139.1529093162; _vwo_uuid_v2=D758DD8172138E6D4A1202C95F2D997D1|82db3a12aece6f72f480e9cbd880e7ba; kampyle_userid=d5e3-50bb-b92f-f3de-1044-be80-8d49-00d6; kampyleUserSession=1529141579299; kampyleSessionPageCounter=2; kampyleUserSessionsCount=6; cd_user_id=164050e0fec10e-095c3b9feb372e-4c312a7a-100200-164050e0fee191; salesforce_id=203789876130cb1c8557a20fffc72b23; recently-viewed=%5B%220596007124%22%2C%229781484233481%3AA323070_2_En_2_Chapter.html%22%2C%221565924185%22%5D; original_referer=\"https://www.google.com/\"; timezoneoffset=-19800; liveagent_oref=https://www.safaribooksonline.com/; liveagent_vc=1; sessionid=5qdqj9wh3t7e82lxzot3byeg3wgwgba4; optimizelyEndUserId=oeu1529141576092r0.4152480509173647; optimizelySegments=%7B%22757067938%22%3A%22direct%22%2C%22778703350%22%3A%22false%22%2C%22781081607%22%3A%22ff%22%2C%22949601412%22%3A%22none%22%7D; optimizelyBuckets=%7B%7D; _gat=1; _uetsid=_uet18e50cb2; _gali=id_email");
        headers.put("DNT", "1");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "");
        headers.put("", "1");

        cookies.put("csrfsafari", "iKVftV8nkEvU4nUaPcSDW7KHFzikensM");
        cookies.put("BrowserCookie", "4a1b25e3-7870-4848-a498-bcd04acea363");
        cookies.put("corp_sessionid", "p37x9dkmc5oczgf33bb8xclskdqa36uk");
        cookies.put("_ga", "GA1.2.1167325580.1529093162");
        cookies.put("_gid", "GA1.2.227569139.1529093162");
        cookies.put("_vwo_uuid_v2", "D758DD8172138E6D4A1202C95F2D997D1|82db3a12aece6f72f480e9cbd880e7ba");
        cookies.put("kampyle_userid", "d5e3-50bb-b92f-f3de-1044-be80-8d49-00d6");
        cookies.put("kampyleUserSession", "1529141579299");
        cookies.put("kampyleSessionPageCounter", "2");
        cookies.put("kampyleUserSessionsCount", "6");
        cookies.put("cd_user_id", "164050e0fec10e-095c3b9feb372e-4c312a7a-100200-164050e0fee191");
        cookies.put("salesforce_id", "203789876130cb1c8557a20fffc72b23");
        cookies.put("recently-viewed", "%5B%220596007124%22%2C%229781484233481%3AA323070_2_En_2_Chapter.html%22%2C%221565924185%22%5D");
        cookies.put("original_referer", "https://www.google.com/");
        cookies.put("timezoneoffset", "-19800");
        cookies.put("liveagent_oref", "https://www.safaribooksonline.com/");
        cookies.put("liveagent_vc", "1");
        cookies.put("sessionid", "5qdqj9wh3t7e82lxzot3byeg3wgwgba4");
        cookies.put("optimizelyEndUserId", "oeu1529141576092r0.4152480509173647");
        cookies.put("optimizelySegments", "%7B%22757067938%22%3A%22direct%22%2C%22778703350%22%3A%22false%22%2C%22781081607%22%3A%22ff%22%2C%22949601412%22%3A%22none%22%7D");
        cookies.put("optimizelyBuckets", "%7B%7D");
        cookies.put("_gat", "1");
        cookies.put("_uetsid", "_uet18e50cb2");
        cookies.put("_gali", "id_email");


        Document testDoc = Jsoup.connect("https://www.safaribooksonline.com/accounts/login/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0")
                .get();
        String csrefToken = testDoc.select("input[name=csrfmiddlewaretoken]").attr("value");

        Connection.Response login = Jsoup.connect("https://www.safaribooksonline.com/accounts/login/")
                .data("csrfmiddlewaretoken", csrefToken)
                .data("dontchange", "http://")
                .data("email", "pijag@fxprix.com")
                .data("is_login_form", "true")
                .data("leaveblank", "")
                .data("password1", "abc#321")
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .cookies(cookies)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0")
                .header("Host", "www.safaribooksonline.com")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en,si;q=0.8,fr;q=0.6,ja;q=0.4,chrome://global/locale/intl.properties;q=0.2")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Referer", "https://www.safaribooksonline.com/")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Content-Length", "174")
//                .header("Cookie", "csrfsafari=iKVftV8nkEvU4nUaPcSDW7KHFzikensM; BrowserCookie=4a1b25e3-7870-4848-a498-bcd04acea363; corp_sessionid=p37x9dkmc5oczgf33bb8xclskdqa36uk; _ga=GA1.2.1167325580.1529093162; _gid=GA1.2.227569139.1529093162; _vwo_uuid_v2=D758DD8172138E6D4A1202C95F2D997D1|82db3a12aece6f72f480e9cbd880e7ba; kampyle_userid=d5e3-50bb-b92f-f3de-1044-be80-8d49-00d6; kampyleUserSession=1529141579299; kampyleSessionPageCounter=2; kampyleUserSessionsCount=6; cd_user_id=164050e0fec10e-095c3b9feb372e-4c312a7a-100200-164050e0fee191; salesforce_id=203789876130cb1c8557a20fffc72b23; recently-viewed=%5B%220596007124%22%2C%229781484233481%3AA323070_2_En_2_Chapter.html%22%2C%221565924185%22%5D; original_referer=\"https://www.google.com/\"; timezoneoffset=-19800; liveagent_oref=https://www.safaribooksonline.com/; liveagent_vc=1; sessionid=5qdqj9wh3t7e82lxzot3byeg3wgwgba4; optimizelyEndUserId=oeu1529141576092r0.4152480509173647; optimizelySegments=%7B%22757067938%22%3A%22direct%22%2C%22778703350%22%3A%22false%22%2C%22781081607%22%3A%22ff%22%2C%22949601412%22%3A%22none%22%7D; optimizelyBuckets=%7B%7D; _gat=1; _uetsid=_uet18e50cb2; _gali=id_email")
                .header("DNT", "1")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .ignoreHttpErrors(true)
                .execute();

//        Jsoup.connect("https://www.safaribooksonline.com/library/view/head-first-design/0596007124/")
//                .data("login:username", username, "login:password", password, "login:loginImg", "", "login", "login")
//                .cookie("sessionid", sessionID)
//                .method(Connection.Method.POST)
//                .timeout(10000)
//                .execute();

        Document doc = Jsoup.connect("https://www.safaribooksonline.com/home/")
                .cookies(login.cookies())
                .timeout(10000)
                .get();
        String source = doc.text();

        Elements elements = doc.select("h1.ChapterTitle");
        String helloworld = "hello world";
    }

}
