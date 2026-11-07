package com.mynote.service;

import com.mynote.enums.InterestEnum;
import com.mynote.restEndPointCaller.RestEndPointCaller;
import com.mynotelibrary.dto.UserRequestDTO;
import com.mynotelibrary.dto.UserResponseDTO;
import com.mynotelibrary.exceptionmessage.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private RestEndPointCaller restEndPointCaller;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public Page<UserResponseDTO> getAllUsers(int page, int size, String sortBy, String order) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", String.valueOf(page));
        params.add("size", String.valueOf(size));

        if (sortBy != null && !sortBy.isBlank())
            params.add("sort", sortBy);

        if (order != null && !order.isBlank())
            params.add("sortDir", order);


        ParameterizedTypeReference<RestResponsePage<UserResponseDTO>> responseType =
                new ParameterizedTypeReference<>() {};

        try {
            RestResponsePage<UserResponseDTO> responsePage = restEndPointCaller.getWithRequestParam(
                    UserEndPoint.GET_USERS,
                    responseType,
                    params
            );

            if (responsePage != null && responsePage.getContent() != null) {
                return new PageImpl<>(
                        responsePage.getContent(),
                        PageRequest.of(page, size > 0 ? size : 10),
                        responsePage.getTotalElements()
                );
            }

            return Page.empty();
        } catch (Exception e) {
            log.error("Error fetching paginated notes: " + e.getMessage());
            return Page.empty();
        }
    }

    public UserResponseDTO updateUser(UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = restEndPointCaller.putCallerWithRequestBody(UserEndPoint.UPDATE_USER,userRequestDTO,UserResponseDTO.class);
        return userResponseDTO;
    }


    public UserResponseDTO getUserById(String userId) {
        UserResponseDTO userResponseDTO = restEndPointCaller.getCallerWithPathVariable(UserEndPoint.GET_USER + "/" + userId, UserResponseDTO.class);
        return userResponseDTO;
    }


    public Page<UserResponseDTO> getAllMatchInterestUser(int page, int size, Set<InterestEnum> interests) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", String.valueOf(page));
        params.add("size", String.valueOf(size));

        for (InterestEnum interest : interests) {
            params.add("interest", interest.getLabel());
        }

        log.info("Interests: {}", params.get("interest"));
        ParameterizedTypeReference<RestResponsePage<UserResponseDTO>> responseType =
                new ParameterizedTypeReference<>() {};

        try {
            RestResponsePage<UserResponseDTO> responsePage = restEndPointCaller.getWithRequestParam(
                    UserEndPoint.GET_MATCH_USER_INTEREST,
                    responseType,
                    params
            );

            if (responsePage != null && responsePage.getContent() != null) {
                log.info("Interests: {}", responsePage.getContent().size());
                log.info("Interests: {}", responsePage.getContent().toArray());
                return new PageImpl<>(
                        responsePage.getContent(),
                        PageRequest.of(page, size > 0 ? size : 10),
                        responsePage.getTotalElements()
                );
            }

            return Page.empty();
        } catch (Exception e) {
            log.error("Error fetching paginated notes: " + e.getMessage());
            return Page.empty();
        }
    }


    public ApiResponse deleteUserById(String userId) {

        ApiResponse apiResponse = restEndPointCaller.deleteCaller(UserEndPoint.DELETE_USER+"/"+userId,ApiResponse.class);
        return apiResponse;
    }


}
