package com.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author colg
 */
@RestController
public class SessionController {


    /**
     * 获取session内容
     *
     * @param request
     * @return
     */
    @GetMapping("/session2")
    public Map<String, Object> map2(HttpServletRequest request) {
        HttpSession session = request.getSession();

        Map<String, Object> map = new HashMap<>(2);
        map.put("sessionId", session.getAttribute("sessionId"));
        map.put("requestURL", session.getAttribute("requestURL"));
        String loginUser = (String) session.getAttribute("loginUser");
//        map.put("loginUser", JSON.toJSONString(loginUser));
        map.put("loginUser", loginUser);
        return map;
    }

}
