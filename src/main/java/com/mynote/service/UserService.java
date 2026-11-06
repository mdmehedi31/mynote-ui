package com.mynote.service;

import com.mynote.restEndPointCaller.RestEndPointCaller;
import com.mynotelibrary.dto.NoteResponseDTO;
import com.mynotelibrary.dto.UserRequestDTO;
import com.mynotelibrary.dto.UserResponseDTO;
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

    public UserResponseDTO crateUser(UserRequestDTO userRequestDTO) {

        UserResponseDTO userResponseDTO = restEndPointCaller.putCallerWithRequestBody(UserEndPoint.UPDATE_USER,userRequestDTO,UserResponseDTO.class);
        return userResponseDTO;
    }
}
