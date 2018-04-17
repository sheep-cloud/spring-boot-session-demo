package com.example.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author colg
 */
@RestController
public class SessionController {

    /**
     * 设置session信息
     * @param request
     * @return
     */
    @GetMapping("/session1")
    public Map<String, Object> map(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>(16);
        User user = new User("Jack", "123456", new Date());

        HttpSession session = request.getSession();
        String sessionId = session.getId();
        StringBuffer requestURL = request.getRequestURL();
        session.setAttribute("sessionId", sessionId);
        session.setAttribute("requestURL", requestURL);
        String loginUser = JSON.toJSONString(user, SerializerFeature.WriteDateUseDateFormat);
        session.setAttribute("loginUser", loginUser);

        map.put("sessionId", sessionId);
        map.put("requestURL", requestURL);
        map.put("loginUser", loginUser);
        return map;
    }

}
