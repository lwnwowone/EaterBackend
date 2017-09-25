package org.spring.Controller;

import com.netflix.governator.annotations.binding.Option;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.http.HttpResponse;
import org.spring.Domain.User;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class LoginController {

    private final JdbcTemplate template;

    public LoginController(JdbcTemplate template) {
        this.template = template;
    }

    @CrossOrigin
    @RequestMapping("/sayHello")
    public String SayHello(){
        return "Hello eater";
    }

    @CrossOrigin
    @PostMapping("/register")
    @ResponseBody
    public String Register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           HttpServletResponse httpServletResponse){
        try {
            List<Map<String,Object>> result = template.queryForList("SELECT * from User Where Username = ?",new Object[]{username});
            if(result.size() > 0) {
                httpServletResponse.setStatus(400);
                return "{ \"errorMessage\" : \"Account has exists\" }";
            }

            String nToken = this.GenerateUUID();
            template.update("INSERT INTO User (Username,Password,AvailableToken) VALUES (?,?,?)",new Object[]{username,password,nToken});
            return "{ \"token\" : \"" + nToken +"\" }";
        }
        catch(Exception ex){
            httpServletResponse.setStatus(400);
            return "{ \"errorMessage\" : \"Database error\" } ";
        }
    }

    @CrossOrigin
    @PostMapping("/login")
    @ResponseBody
    public String Login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpServletResponse httpServletResponse){

        List<Map<String,Object>> result = template.queryForList("SELECT * from User Where Username = ?",new Object[]{username});
        if(result.size() > 0){
            Map<String,Object> resultMap = result.get(0);
            String rPassword = resultMap.get("Password").toString();
            if(rPassword.equals(password)){
                String nToken = this.GenerateUUID();
                try {
                    template.update("UPDATE User SET AvailableToken = ? WHERE Username = ?",new Object[]{nToken,username});
                    return "{ \"token\" : \""+ nToken + "\" }";
                }
                catch(Exception ex){
                    httpServletResponse.setStatus(400);
                    return "{ \"errorMessage\" : \"DataBase error\" }";
                }
            }
            httpServletResponse.setStatus(400);
            return "{ \"errorMessage\" : \"Incorrect password\" }";
        }
        httpServletResponse.setStatus(400);
        return "{ \"errorMessage\" : \"Account is not exists\" }";
    }



    private String GenerateUUID(){
        UUID uuid=UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr=str.replace("-", "");
        return uuidStr;
    }
}
