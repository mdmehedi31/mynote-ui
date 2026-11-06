package com.mynote.service;

import com.mynote.restEndPointCaller.RestEndPointCaller;
import com.mynotelibrary.dto.NoteRequestDTO;
import com.mynotelibrary.dto.NoteResponseDTO;
import com.mynotelibrary.exceptionmessage.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class NoteService {

    private RestEndPointCaller restEndPointCaller;
    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    public NoteService(RestEndPointCaller restEndPointCaller) {
        this.restEndPointCaller = restEndPointCaller;
    }


    public NoteResponseDTO createNote(NoteRequestDTO noteRequestDTO) {
        NoteResponseDTO noteResponseDTO = null;

        if (noteRequestDTO.getId() == null) {
           noteResponseDTO= restEndPointCaller.postCallerWithRequestBody(NoteEndPointPath.CREATE_NOTE_PATH,noteRequestDTO,NoteResponseDTO.class);
        }else if(noteRequestDTO.getId()!=null){
            noteResponseDTO=restEndPointCaller.putCallerWithRequestBody(NoteEndPointPath.EDIT_NOTE_PATH,noteRequestDTO,NoteResponseDTO.class);
        }
        return noteResponseDTO;
    }


    public Page<NoteResponseDTO> getUserAllNotes(String searchKeyword, int page, int size, String userId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", String.valueOf(page));
        params.add("size", String.valueOf(size));
        params.add("userId", userId);

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            params.add("search", searchKeyword);
        }

        ParameterizedTypeReference<RestResponsePage<NoteResponseDTO>> responseType =
                new ParameterizedTypeReference<>() {};

        try {
            RestResponsePage<NoteResponseDTO> responsePage = restEndPointCaller.getWithRequestParam(
                    NoteEndPointPath.LIST_NOTE_PATH,
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


    public ApiResponse deleteById(String noteId) {

        ApiResponse apiResponse= restEndPointCaller.deleteCaller(NoteEndPointPath.DELETE_NOTE_PATH+"/"+noteId, ApiResponse.class);
        return apiResponse;
    }
}
