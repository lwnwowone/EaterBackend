package org.spring.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
public class ExplanationController {

    private static final Logger log = LoggerFactory.getLogger(ExplanationController.class);

    @CrossOrigin
    @GetMapping("/apiList")
    public String APIList() throws IOException {
        String apiList = "";
        InputStream stream = getClass().getResourceAsStream("/apiList.txt");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            // do something
            String lineTxt = null;
            while ((lineTxt = reader.readLine()) != null) {
                apiList += lineTxt + "\n";
            }
            reader.close();
        }

        return apiList;
    }

}
