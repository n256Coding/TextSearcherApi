package com.n256coding.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/video", "/text-request", "/text-responce", "/moodle", "/slide"})
    public String getPage() {
        return "forward:/index.html";
    }
}
