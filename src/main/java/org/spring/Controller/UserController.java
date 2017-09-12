package org.spring.Controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private final JdbcTemplate template;

    public UserController(JdbcTemplate template) {
        this.template = template;
    }

    @CrossOrigin
    @PostMapping("/password/change")
    @ResponseBody
    public String Login(@RequestParam("oPassword") String oPassword,
                        @RequestParam("nPassword") String nPassword,
                        @RequestHeader("token") String token,
                        HttpServletResponse httpServletResponse){
        if(!token.isEmpty()) {
            List<Map<String,Object>> result = template.queryForList("SELECT * from User Where AvailableToken = ?",new Object[]{token});
            if(result.size() > 0){
                Map<String,Object> resultMap = result.get(0);
                String rPassword = resultMap.get("Password").toString();
                if(rPassword.equals(oPassword)){
                    try {
                        template.update("UPDATE User SET Password = ? WHERE AvailableToken = ?",new Object[]{nPassword,token});
                        return "";
                    }
                    catch(Exception ex){
                        httpServletResponse.setStatus(400);
                        return "{ \"errorMessage\" : \"DataBase error\" }";
                    }
                }
                httpServletResponse.setStatus(400);
                return "{ \"errorMessage\" : \"Original password is incorrect\" }";
            }
            httpServletResponse.setStatus(400);
            return "{ \"errorMessage\" : \"Account is not exists\" }";
        }
        return "{ \"errorMessage\" : \"Token invalid\" } ";
    }

    @CrossOrigin
    @GetMapping("/profile")
    @ResponseBody
    public String Profile(@RequestHeader("token") String token,
                          HttpServletResponse httpServletResponse) throws Exception {
        if(!token.isEmpty()) {
            List<Map<String,Object>> result = template.queryForList("SELECT * from User Where AvailableToken = ?",new Object[]{token});
            if(result.size() > 0){
                Map<String,Object> resultMap = result.get(0);
                String username = resultMap.get("Username").toString();
                String nickname = resultMap.get("Nickname").toString();
                String department = resultMap.get("Department").toString();
                String hasOrdered = HasOrderedLocal(token);
                hasOrdered = hasOrdered.contains("true") ? "true" : "false";
                return "{ \"username\" : \"" + username + "\", \"nickname\" : \"" + nickname + "\", \"department\" : \"" + department + "\", \"hasOrdered\" : \"" + hasOrdered + "\" }";
            }
            httpServletResponse.setStatus(400);
            return "{ \"errorMessage\" : \"Account is not exists\" }";
        }
        return "{ \"errorMessage\" : \"Token invalid\" } ";
    }

    @CrossOrigin
    @PostMapping("/profile/modify")
    @ResponseBody
    public String ProfileModify(@RequestHeader("token") String token,
                                @RequestParam("nickname") String nNickname,
                                @RequestParam("department") String nDepartment,
                                HttpServletResponse httpServletResponse) throws Exception{
        if(!token.isEmpty()) {
            List<Map<String,Object>> result = template.queryForList("SELECT * from User Where AvailableToken = ?",new Object[]{token});
            if(result.size() > 0){
                template.update("UPDATE User SET Nickname = ?,Department = ? WHERE AvailableToken = ? ",new Object[]{nNickname,nDepartment,token});
                return "";
            }
            httpServletResponse.setStatus(400);
            return "{ \"errorMessage\" : \"Account is not exists\" }";
        }
        return "{ \"errorMessage\" : \"Token invalid\" } ";
    }

    //TODO Move it to order
    @CrossOrigin
    @GetMapping("/hasOrdered")
    @ResponseBody
    public String HasOrdered(@RequestHeader("token") String token,
                             HttpServletResponse httpServletResponse) throws IOException{
        String result = HasOrderedLocal(token);
        if(result.contains("errorMessage")) {
            httpServletResponse.setStatus(400);
        }
        return result;
    }

    private String HasOrderedLocal(String token) throws IOException{
        if(!token.isEmpty()) {
            List<Map<String,Object>> result = template.queryForList("SELECT * from User Where AvailableToken = ?",new Object[]{token});
            if(result.size() > 0){
                Map<String,Object> resultMap = result.get(0);
                String username = resultMap.get("Username").toString();
                try {
                    result = template.queryForList("SELECT * FROM UserOrders WHERE Username = ? AND OrderDate = ? AND Alive = 1",new Object[]{username,GetCurrentDate()});
                    return (result.size() > 0) ? "{ \"result\" : \"true\" }" : "{ \"result\" : \"false\" }";
                }
                catch(Exception ex){
                    return "{ \"errorMessage\" : \"DataBase error\" }";
                }
            }
            return "{ \"errorMessage\" : \"Account is not exists\" }";
        }
        return "{ \"errorMessage\" : \"Token invalid\" } ";
    }

    private String GetCurrentDate(){
        Calendar now = Calendar.getInstance();

        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        String result = FixedStringValueOf(year) + FixedStringValueOf(month) + FixedStringValueOf(day);
        return result;
    }

    private String FixedStringValueOf(int intValue){
        String result = String.valueOf(intValue);
        if(1 == result.length())
            result = "0" + result;
        return result;
    }
}
