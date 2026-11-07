package com.mynote.service;


import com.mynote.restEndPointCaller.RestEndPointCaller;
import com.mynotelibrary.dto.UserAuthInfo;
import com.mynotelibrary.dto.UserRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

    @Autowired
    private RestEndPointCaller restEndPointCaller;

    public UserAuthInfo userLogin(String username, String password) {
        UserRequestDTO req = new UserRequestDTO();
        req.setEmail(username);
        req.setPassword(password);

        UserAuthInfo userAuthInfo = restEndPointCaller.postCallerWithRequestBody(UserAuthEndpoint.LOGIN,req,UserAuthInfo.class);
        return userAuthInfo;
    }

    public UserAuthInfo userCreate(UserRequestDTO userRequestDTO) {
        UserAuthInfo userAuthInfo = restEndPointCaller.postCallerWithRequestBody(UserAuthEndpoint.CREATE,userRequestDTO,UserAuthInfo.class);
        return userAuthInfo;
    }



}
