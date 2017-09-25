package org.spring.Controller;

import org.spring.Domain.MenuItem;
import org.spring.Domain.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class MenuController {

    private final JdbcTemplate template;

    public MenuController(JdbcTemplate template) {
        this.template = template;
    }

    @CrossOrigin
    @GetMapping("/menu/availableItemList")
    @ResponseBody
    public String GetAvailableMenu(HttpServletResponse httpServletResponse,
                            @RequestHeader("token") String token){
        int userAccess = GetUserAccessFromToken(token);
        if(userAccess >= 0) {
            try {
                List<Map<String, Object>> result = template.queryForList("SELECT * from MenuBook");
                if (result.size() > 0) {
                    List<MenuItem> orderList = new ArrayList<>();
                    for (Map<String,Object> tMap : result){
                        MenuItem tItem = new MenuItem();
                        tItem.setItemID(tMap.get("ItemID").toString());
                        tItem.setItemName(tMap.get("ItemName").toString());
                        tItem.setItemDescription(tMap.get("ItemDescription").toString());
                        tItem.setAvailable(tMap.get("Available").toString());
                        orderList.add(tItem);
                    }
                    String orderListJson = ToJsonString(orderList);
                    return "{ \"orderList\" : " + orderListJson + " } ";
                }
                return "{ \"menuList\" : {} } ";
            }
            catch(Exception ex){
                httpServletResponse.setStatus(400);
                return "{ \"errorMessage\" : \"Operation failed, error message is : " + ex.getMessage() + "\" } ";
            }
        }
        return "{ \"errorMessage\" : \"Access denied\" } ";
    }

    @CrossOrigin
    @PostMapping("/menu/create")
    @ResponseBody
    public String CreateItem(@RequestParam("name") String iName,
                              @RequestParam("description") String iDescription,
                              @RequestHeader("token") String token,
                              HttpServletResponse httpServletResponse){
        int userAccess = GetUserAccessFromToken(token);
        if(userAccess > 0) {
            try {
                template.update("INSERT INTO MenuBook (ItemName,ItemDescription) VALUES (?,?)", new Object[]{iName, iDescription});
                return "";
            } catch (Exception ex) {
                httpServletResponse.setStatus(400);
                return "{ \"errorMessage\" : \"Operation failed, error message is : " + ex.getMessage() + "\" } ";
            }
        }
        return "{ \"errorMessage\" : \"Access denied\" } ";
    }

    @CrossOrigin
    @PostMapping("/menu/remove")
    @ResponseBody
    public String RemoveItem(@RequestParam("itemID") String iID,
                              @RequestHeader("token") String token,
                              HttpServletResponse httpServletResponse){
        int userAccess = GetUserAccessFromToken(token);
        if(userAccess > 0) {
            List<Map<String, Object>> result = template.queryForList("SELECT * from MenuBook Where ItemID = ?", new Object[]{iID});
            if (result.size() > 0) {
                try {
                    template.update("DELETE FROM MenuBook WHERE ItemID = ? ",new Object[]{iID});
                    return "";
                } catch (Exception ex) {
                    httpServletResponse.setStatus(400);
                    return "{ \"errorMessage\" : \"Operation failed, error message is : " + ex.getMessage() + "\" } ";
                }
            }
            httpServletResponse.setStatus(400);
            return "{ \"errorMessage\" : \"Item is not exists \" }";
        }
        return "{ \"errorMessage\" : \"Access denied\" } ";
    }

    @CrossOrigin
    @PostMapping("/menu/deactivate")
    @ResponseBody
    public String DeactivateOrder(@RequestParam("itemID") String iID,
                                  @RequestHeader("token") String token,
                                  HttpServletResponse httpServletResponse){
        int userAccess = GetUserAccessFromToken(token);
        if(userAccess > 0) {
            List<Map<String, Object>> result = template.queryForList("SELECT * from MenuBook Where ItemID = ? AND Available = 1", new Object[]{iID});
            if (result.size() > 0) {
                try {
                    template.update("UPDATE MenuBook SET Available = ? WHERE ItemID = ?",new Object[]{0,iID});
                    return "";
                } catch (Exception ex) {
                    httpServletResponse.setStatus(400);
                    return "{ \"errorMessage\" : \"Operation failed, error message is : " + ex.getMessage() + "\" } ";
                }
            }
            httpServletResponse.setStatus(400);
            return "{ \"errorMessage\" : \"Item is not exists(available) \" }";
        }
        return "{ \"errorMessage\" : \"Access denied\" } ";
    }

    private String ToJsonString(List<MenuItem> list){
        String result = "[";
        for (MenuItem tItem : list){
            String ObjectJson = "{";
            ObjectJson += "\"ItemID\" : \"" + tItem.getItemID() + "\",";
            ObjectJson += "\"ItemName\" : \"" + tItem.getItemName() + "\",";
            ObjectJson += "\"ItemDescription\" : \"" + tItem.getItemDescription() + "\",";
            ObjectJson += "\"Available\" : \"" + tItem.getAvailable() + "\",";
            ObjectJson += "}";
            result += ObjectJson + ",";
        }
        if(list.size() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        result += "]";
        return result;
    }

    private int GetUserAccessFromToken(String token){
        int result = -1;
        try{
            List<Map<String,Object>> qResult = template.queryForList("SELECT * from User Where AvailableToken = ?",new Object[]{token});
            if(qResult.size() > 0) {
                Map<String, Object> obj0 = qResult.get(0);
                String StrResult = obj0.get("UserAccess").toString();
                result = Integer.valueOf(StrResult);
            }
            return result;
        }
        catch (Exception ex){
            return result;
        }
    }
}
