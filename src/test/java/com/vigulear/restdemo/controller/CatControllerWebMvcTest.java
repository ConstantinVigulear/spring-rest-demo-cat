package com.vigulear.restdemo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import com.vigulear.restdemo.dto.CatDto;
import com.vigulear.restdemo.entity.Cat;
import com.vigulear.restdemo.exceptions.InvalidValueException;
import com.vigulear.restdemo.mapper.CatMapper;
import com.vigulear.restdemo.service.CatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author : crme059
 * @created : 01-Dec-23, Friday
 */
@WebMvcTest(CatController.class) // setups mockmvc and objectMapper
class CatControllerWebMvcTest {

  @Autowired MockMvc mockMvc;

  @MockBean CatService catService; // @MockBean integrates catService into Spring

  @Autowired ObjectMapper objectMapper;

  @Test
  @DisplayName("POST /cat/create returns new catDto and Http 201 Created ")
  void createCat_returnsValidEntity() throws Exception {
    Cat cat = Cat.builder().id(1001L).name("Couscous").age(10).build();
    String catJson = objectMapper.writeValueAsString(cat);
    CatDto catDto = CatMapper.mapToCatDto(cat);
    String catDtoJson = objectMapper.writeValueAsString(catDto);

    when(catService.createCat(cat)).thenReturn(catDto);

    mockMvc
        .perform(post("/cat/create").contentType(MediaType.APPLICATION_JSON).content(catJson))
        .andExpectAll(
            status().isCreated(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(catDtoJson),
            jsonPath("$.id").value(cat.getId()),
            jsonPath("$.name").value(cat.getName()),
            jsonPath("$.age").value(cat.getAge()));

    verify(catService, times(1)).createCat(any());
  }

  @Test
  @DisplayName("PUT /cat/update/{id} with valid Payload returns Http 200 Ok")
  void updateById_validPayload_returnsUpdatedCatDto() throws Exception {
    Cat catToUpdate = Cat.builder().id(1001L).name("Millefoglie").age(10).build();
    String catToUpdateJson = objectMapper.writeValueAsString(catToUpdate);
    CatDto catToUpdateDto = CatMapper.mapToCatDto(catToUpdate);
    String catToUpdateDtoJson = objectMapper.writeValueAsString(catToUpdateDto);

    when(catService.updateById(catToUpdate.getId(), catToUpdate)).thenReturn(catToUpdateDto);
    mockMvc
        .perform(
            put("/cat/update/{id}", catToUpdate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(catToUpdateJson))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(catToUpdateDtoJson),
            jsonPath("$.name").value("Millefoglie"));
  }

  @Test
  @DisplayName(
      "PUT /cat/update/{id} with invalid id throws InvalidValueException Http 400 Bad Request")
  void updateById_invalidId_throwsInvalidValueException() throws Exception {
    Cat catToUpdate = Cat.builder().id(-1L).name("Millefoglie").age(10).build();
    String catToUpdateJson = objectMapper.writeValueAsString(catToUpdate);

    when(catService.updateById(catToUpdate.getId(), catToUpdate))
        .thenThrow(
            new InvalidValueException("There is no cat with id = " + catToUpdate.getId()));
    mockMvc
        .perform(
            put("/cat/update/{id}", catToUpdate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(catToUpdateJson))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string("There is no cat with id = " + catToUpdate.getId()));
  }

  @Test
  @DisplayName("GET /cat/{id} with valid Payload returns catDto and Http 201 Ok ")
  void getCatById_validPayload_returnCatDto() throws Exception {
    Cat cat = Cat.builder().id(1001L).name("Couscous").age(10).build();
    CatDto catDto = CatMapper.mapToCatDto(cat);
    String catDtoJson = objectMapper.writeValueAsString(catDto);

    when(catService.findById(cat.getId())).thenReturn(catDto);

    mockMvc
        .perform(get("/cat/{id}", cat.getId()).contentType(MediaType.TEXT_PLAIN_VALUE))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(catDtoJson),
            jsonPath("$.id").value(cat.getId()),
            jsonPath("$.name").value(cat.getName()),
            jsonPath("$.age").value(cat.getAge()));

    verify(catService, times(1)).findById(any());
  }

  @Test
  @DisplayName(
      "GET /cat/{id} with invalid id throws InvalidValueException and HTTP 400 Bad Request")
  void getCatById_validPayload_throwsInvalidValueException() throws Exception {
    Long id = -1L;

    when(catService.findById(id)).thenReturn(null);

    mockMvc
        .perform(get("/cat/{id}", id))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string("Cat with id = '" + id + "' is not found"));

    verify(catService, times(1)).findById(any());
  }

  @Test
  @DisplayName(
      "GET /cat/{id} with invalid Payload throws InvalidValueException and Http 400 Bad Request")
  void getCatById_invalidPayload_throwsInvalidValueException() throws Exception {
    String id = "*";

    mockMvc
        .perform(get("/cat/{id}", id))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string("Invalid value \"" + id + "\" for parameter"));

    verify(catService, times(0)).findById(any());
  }

  @Test
  @DisplayName("GET /cat returns list of cat dtos and Http 200 Ok")
  void getAllCats() throws Exception {
    List<Cat> cats =
        List.of(
            Cat.builder().id(1001L).name("Couscous").age(0).build(),
            Cat.builder().id(1002L).name("Tiramisu").age(7).build());
    List<CatDto> catDtos = cats.stream().map(CatMapper::mapToCatDto).toList();

    when(catService.findAll()).thenReturn(catDtos);

    mockMvc
        .perform(get("/cat"))
        .andExpectAll(status().isOk(), content().json(objectMapper.writeValueAsString(catDtos)));
    verify(catService, times(1)).findAll();
  }

  @Test
  @DisplayName(
      "GET /cat/?top={top}&fieldName={fieldName} with valid Payload returns list of cat dtos and Http 200 Ok")
  void getTopByField_validPayload_returnValidListOfCatDtos() throws Exception {
    Integer top = 2;
    String fieldName = "age";
    List<Cat> cats =
        List.of(
            Cat.builder().id(1001L).name("Couscous").age(0).build(),
            Cat.builder().id(1002L).name("Tiramisu").age(7).build());
    List<CatDto> catDtos = cats.stream().map(CatMapper::mapToCatDto).toList();

    when(catService.findTopByField(top, fieldName)).thenReturn(catDtos);

    mockMvc
        .perform(get("/cat/?top={top}&fieldName={fieldName}", top, fieldName))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(objectMapper.writeValueAsString(catDtos)));
    verify(catService, times(1)).findTopByField(any(), any());
  }

  @Test
  @DisplayName(
      "GET /cat/?top={top}&fieldName={fieldName} with invalid Top throws InvalidValueException and Http 400 Bad Request")
  void getTopByField_invalidTop_throwsInvalidValueException() throws Exception {
    Integer top = -2;
    String fieldName = "any";

    mockMvc
        .perform(get("/cat/?top={top}&fieldName={fieldName}", top, fieldName))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string("Invalid value \"" + top + "\" for parameter \"top\""));
    verify(catService, times(0)).findTopByField(any(), any());
  }

  @Test
  @DisplayName(
      "GET /cat/?top={top}&fieldName={fieldName} with invalid fieldName throws InvalidValueException and Http 400 Bad Request")
  void getTopByField_invalidFieldName_throwsInvalidValueException() throws Exception {
    Integer top = 2;
    String fieldName = "invalid";

    mockMvc
        .perform(get("/cat/?top={top}&fieldName={fieldName}", top, fieldName))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string("No such field as '" + fieldName + "'"));
    verify(catService, times(0)).findTopByField(any(), any());
  }

  @Test
  @DisplayName(
      "GET /cat/top3?fieldName={fieldName} with valid fieldName returns list of cat dtos and Http 200 Ok")
  void getTopThree_validFieldName_returnValidListOfCatDtos() throws Exception {
    String fieldName = "age";
    Sort sort = Sort.by(fieldName).ascending();
    List<Cat> cats =
        List.of(
            Cat.builder().id(1001L).name("Couscous").age(0).build(),
            Cat.builder().id(1002L).name("Tiramisu").age(7).build(),
            Cat.builder().id(1003L).name("Humus").age(1).build());
    List<CatDto> catDtos = cats.stream().map(CatMapper::mapToCatDto).toList();

    when(catService.findFirst3(sort)).thenReturn(catDtos);

    mockMvc
        .perform(get("/cat/top3?fieldName={fieldName}", fieldName))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(objectMapper.writeValueAsString(catDtos)));
    verify(catService, times(1)).findFirst3(sort);
  }

  @Test
  @DisplayName(
      "GET /cat/top3?fieldName={fieldName} with invalid fieldName throws InvalidValueException and Http 400 Bad Request")
  void getTopThree_invalidFieldName_throwsInvalidValueException() throws Exception {
    String fieldName = "diet";
    Sort sort = Sort.by(fieldName).ascending();

    mockMvc
        .perform(get("/cat/top3?fieldName={fieldName}", fieldName))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string("No such field as '" + fieldName + "'"));
    verify(catService, times(0)).findFirst3(sort);
  }

  @Test
  @DisplayName("GET /cat/youngest returns valid cat and Http 200 Ok")
  void findTheYoungest_returnValidCat() throws Exception {
    var cat = Cat.builder().id(1001L).name("Couscous").age(0).build();
    var catDto = CatMapper.mapToCatDto(cat);
    String catDtoJson = objectMapper.writeValueAsString(catDto);
    when(catService.findFirstByOrderByAge()).thenReturn(catDto);

    mockMvc
        .perform(get("/cat/youngest"))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(catDtoJson),
            jsonPath("$.id").value(cat.getId()),
            jsonPath("$.name").value(cat.getName()),
            jsonPath("$.age").value(cat.getAge()));
    verify(catService, times(1)).findFirstByOrderByAge();
  }

  @Test
  @DisplayName("GET /cat/youngest throws InvalidValueException and Http 400 Bad Request")
  void findTheYoungest_throwsInvalidValueException() throws Exception {

    when(catService.findFirstByOrderByAge()).thenReturn(null);

    mockMvc
        .perform(get("/cat/youngest"))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string("There are no records in data base"));

    verify(catService, times(1)).findFirstByOrderByAge();
  }

  @Test
  @DisplayName(
      "GET /cat/total?fieldName={fieldName} with valid fieldName returns valid total and Http 200 Ok")
  void findTotalBy_validFieldName_returnValidTotal() throws Exception {
    String fieldName = "age";
    Integer expectedTotal = 123;
    when(catService.findTotalBy(fieldName)).thenReturn(expectedTotal);

    mockMvc
        .perform(get("/cat/total?fieldName={fieldName}", fieldName))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(objectMapper.writeValueAsString(expectedTotal)));
    verify(catService, times(1)).findTotalBy(fieldName);
  }

  @Test
  @DisplayName(
      "GET /cat/total?fieldName={fieldName} with invalid fieldName throws InvalidValueException and Http 400 Bad Request")
  void findTotalBy_invalidFieldName_throwsInvalidValueException() throws Exception {
    String fieldName = "name";

    mockMvc
        .perform(get("/cat/total?fieldName={fieldName}", fieldName))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string("No such field as '" + fieldName + "'"));
    verify(catService, times(0)).findTotalBy(any());
  }

  @Test
  @DisplayName("DELETE /cat/delete/{id} with valid id returns Http 204 No Content")
  void deleteById_validId_returnsDeletedCatDto() throws Exception {
    var catToDelete = Cat.builder().id(1L).build();

    doNothing().when(catService).deleteById(catToDelete.getId());

    mockMvc
        .perform(delete("/cat/delete/{id}", catToDelete.getId()))
        .andExpect(
            status().isNoContent());
    verify(catService, times(1)).deleteById(any());
  }

  @Test
  @DisplayName(
      "DELETE /cat/delete/{id} with invalid id throws InvalidValueException and Http 400 Bad Request")
  void deleteById_invalidId_throwsInvalidValueException() throws Exception {
    Long id = -1L;
    String errorMessage = "There is no cat with id = " + id;

    doThrow(new InvalidValueException("There is no cat with id = " + id))
        .when(catService)
        .deleteById(id);

    mockMvc
        .perform(delete("/cat/delete/{id}", id))
        .andExpectAll(
            status().isBadRequest(),
            content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"),
            content().string(errorMessage));
  }
}
