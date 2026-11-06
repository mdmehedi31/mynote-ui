package com.mynote.service;


import com.mynote.restEndPointCaller.RestEndPointCaller;
import com.mynotelibrary.dto.NoteResponseDTO;
import com.mynotelibrary.dto.UserPostRequestDTO;
import com.mynotelibrary.dto.UserPostResponse;
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
public class PostService {

    @Autowired
    private RestEndPointCaller restEndPointCaller;
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    public Page<UserPostResponse> getUserAllNotes(int page, int size) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", String.valueOf(page));
        params.add("size", String.valueOf(size));

        ParameterizedTypeReference<RestResponsePage<UserPostResponse>> responseType =
                new ParameterizedTypeReference<>() {};

        try {
            RestResponsePage<UserPostResponse> responsePage = restEndPointCaller.getWithRequestParam(
                    UserPostEndPoint.USER_POSTS,
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

    public UserPostResponse createPost(UserPostRequestDTO userPostRequestDTO) {

        UserPostResponse userPostResponse = restEndPointCaller.postCallerWithRequestBody(UserPostEndPoint.CREATE_POST, userPostRequestDTO, UserPostResponse.class);
        return userPostResponse;
    }

}
