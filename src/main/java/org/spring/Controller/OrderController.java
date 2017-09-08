package org.spring.Controller;

import org.spring.Domain.Order;
import org.spring.Domain.User;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    private final JdbcTemplate template;

    public OrderController(JdbcTemplate template) {
        this.template = template;
    }

    @CrossOrigin
    @GetMapping("/order/todayOrders")
    @ResponseBody
    public String GetOrders(HttpServletResponse httpServletResponse,
                            @RequestHeader("token") String token){
        String username = GetUsernameFromToken(token);
        if(!token.isEmpty() && !username.isEmpty()) {
            String currentDate = GetCurrentDate();
            try {
                List<Map<String, Object>> result = template.queryForList("SELECT * from UserOrders Where OrderDate = ? AND Alive = ?", new Object[]{currentDate,1});
                if (result.size() > 0) {
                    List<Order> orderList = new ArrayList<>();
                    for (Map<String,Object> tMap : result){
                        Order tOrder = new Order();
                        tOrder.setOrderID(tMap.get("OrderID").toString());
                        tOrder.setUsername(tMap.get("Username").toString());
                        tOrder.setUserNickname(tMap.get("UserNickname").toString());
                        tOrder.setOrderTime(tMap.get("OrderTime").toString());
                        tOrder.setOrderDate(tMap.get("OrderDate").toString());
                        orderList.add(tOrder);
                    }
                    String orderListJson = ToJsonString(orderList);
                    return "{ \"orderList\" : " + orderListJson + " } ";
                }
                return "{ \"orderList\" : {} } ";
            }
            catch(Exception ex){
                httpServletResponse.setStatus(400);
                return "{ \"errorMessage\" : \"Operation failed, error message is : " + ex.getMessage() + "\" } ";
            }
        }
        return "{ \"errorMessage\" : \"Token invalid\" } ";
    }

    @CrossOrigin
    @PostMapping("/order/create")
    @ResponseBody
    public String CreateOrder(@RequestHeader("token") String token,
                              HttpServletResponse httpServletResponse){
        String username = GetUsernameFromToken(token);
        if(!token.isEmpty() && !username.isEmpty()) {
            String currentDate = GetCurrentDate();
            try {
                List<Map<String, Object>> result = template.queryForList("SELECT * from UserOrders Where Username = ? AND OrderDate = ?", new Object[]{username,currentDate});
                if (result.size() > 0) {
                    httpServletResponse.setStatus(400);
                    return "{ \"errorMessage\" : \"Order has exists\" }";
                }
                String currentTime = GetCurrentTime();
                template.update("INSERT INTO UserOrders (Username,UserNickname,OrderTime,OrderDate) VALUES (?,?,?,?)", new Object[]{username, GetUserNicknameFromToken(token), currentTime, currentDate});
                return "";
            } catch (Exception ex) {
                httpServletResponse.setStatus(400);
                return "{ \"errorMessage\" : \"Operation failed, error message is : " + ex.getMessage() + "\" } ";
            }
        }
        return "{ \"errorMessage\" : \"Token invalid\" } ";
    }

    @CrossOrigin
    @PostMapping("/order/remove")
    @ResponseBody
    public String RemoveOrder(@RequestHeader("token") String token,
                              HttpServletResponse httpServletResponse){
        String username = GetUsernameFromToken(token);
        if(!token.isEmpty() && !username.isEmpty()) {
            String currentDate = GetCurrentDate();
            List<Map<String, Object>> result = template.queryForList("SELECT * from UserOrders Where Username = ? AND OrderDate = ?", new Object[]{username,currentDate});
            if (result.size() > 0) {
                try {
                    template.update("UPDATE UserOrders SET Alive = ? WHERE Username = ?",new Object[]{0,username});
                    return "";
                } catch (Exception ex) {
                    httpServletResponse.setStatus(400);
                    return "{ \"errorMessage\" : \"Operation failed, error message is : " + ex.getMessage() + "\" } ";
                }
            }
            httpServletResponse.setStatus(400);
            return "{ \"errorMessage\" : \"Order is not exists\" }";
        }
        return "{ \"errorMessage\" : \"Token invalid\" } ";
    }

    private String GetUserNicknameFromToken(String token){
        String result = null;
        try{
            List<Map<String,Object>> qResult = template.queryForList("SELECT * from User Where AvailableToken = ?",new Object[]{token});
            if(qResult.size() > 0) {
                Map<String, Object> obj0 = qResult.get(0);
                result = obj0.get("Nickname").toString();
            }
            return result;
        }
        catch (Exception ex){
            return result;
        }
    }

    private String GetUsernameFromToken(String token){
        String result = "";
        try{
            List<Map<String,Object>> qResult = template.queryForList("SELECT * from User Where AvailableToken = ?",new Object[]{token});
            if(qResult.size() > 0) {
                Map<String, Object> obj0 = qResult.get(0);
                result = obj0.get("Username").toString();
            }
            return result;
        }
        catch (Exception ex){
            return result;
        }
    }

    private String ToJsonString(List<Order> orderList){
        String result = "[";
        for (Order tOrder : orderList){
            String ObjectJson = "{";
            ObjectJson += "\"OrderID\" : \"" + tOrder.getOrderID() + "\",";
            ObjectJson += "\"Username\" : \"" + tOrder.getUsername() + "\",";
            ObjectJson += "\"UserNickname\" : \"" + tOrder.getUserNickname() + "\",";
            ObjectJson += "\"OrderTime\" : \"" + tOrder.getOrderTime() + "\",";
            ObjectJson += "\"OrderDate\" : \"" + tOrder.getOrderDate() + "\"";
            ObjectJson += "}";
            result += ObjectJson + ",";
        }
        if(orderList.size() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        result += "]";
        return result;
    }

    private String GetCurrentDate(){
        Calendar now = Calendar.getInstance();

        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        String result = FixedStringValueOf(year) + FixedStringValueOf(month) + FixedStringValueOf(day);
        return result;
    }

    private String GetCurrentTime(){
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);

        String result = FixedStringValueOf(hour) + FixedStringValueOf(minute) + FixedStringValueOf(second);
        return result;
    }

    private String FixedStringValueOf(int intValue){
        String result = String.valueOf(intValue);
        if(1 == result.length())
            result = "0" + result;
        return result;
    }
}
