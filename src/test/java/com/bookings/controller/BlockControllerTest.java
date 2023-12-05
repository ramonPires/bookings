package com.bookings.controller;

import com.bookings.exception.BusinessException;
import com.bookings.exception.ErrorCode;
import com.bookings.exception.PropertyAlreadyBlockedException;
import com.bookings.models.Block;
import com.bookings.models.Property;
import com.bookings.service.BlockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.bookings.controller.ControllerTestUtils.TIMESTAMP_REGEX;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlockController.class)
class BlockControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BlockService blockService;

    @Autowired
    ObjectMapper mapper;

    @Test
    @DisplayName("Should fail to create a block and return a status code 400 if block dates are invalid")
    public void shouldFailToCreateABlockIfBlockDatesAreInvalid() throws Exception {
        Block block = new Block(null, null, new Property(1L));

        mockMvc.perform(post("/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(block))
                )
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.errors.startDate").value("Start date is required"))
                .andExpect(jsonPath("$.errors.endDate").value("End date is required"))
                .andExpect(status().isBadRequest());
        verify(blockService, never()).createBlock(block);
    }

    @Test
    @DisplayName("Should fail to create a block and return a status code 400 if property id is not defined")
    public void shouldFailToCreateABlockIfPropertyIsNotDefined() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 4, 10);
        LocalDate endDate = LocalDate.of(2024, 4, 12);
        Block block = new Block(startDate, endDate, new Property(null));

        mockMvc.perform(post("/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(block))
                )
                .andDo(print())
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.errors.property").value("Property is required"))
                .andExpect(status().isBadRequest());
        verify(blockService, never()).createBlock(block);
    }

    @Test
    @DisplayName("Should fail to create a block and return a status code 422 if property is blocked")
    public void shouldFailToCreateABlockIfPropertyIsBlocked() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 4, 10);
        LocalDate endDate = LocalDate.of(2024, 4, 12);
        Block block = new Block(startDate, endDate, new Property(1L));
        PropertyAlreadyBlockedException exception = new PropertyAlreadyBlockedException(block);
        when(blockService.createBlock(any(Block.class))).thenThrow(exception);

        mockMvc.perform(post("/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(block))
                )
                .andDo(print())
                .andExpect(jsonPath("$.errorCode").value(exception.getErrorCode().name()))
                .andExpect(jsonPath("$.message").value(exception.getMessage()))
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(status().isUnprocessableEntity());
        verify(blockService).createBlock(any(Block.class));
    }

    @Test
    @DisplayName("Should create a block and return a status code 201")
    public void shouldCreateABlockWithSuccess() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 4, 10);
        LocalDate endDate = LocalDate.of(2024, 4, 12);
        Block newBlock = new Block(startDate, endDate, new Property(1L));

        Block createdBlock = new Block(startDate, endDate, new Property(1L));
        createdBlock.setId(1L);

        when(blockService.createBlock(newBlock)).thenReturn(createdBlock);

        mockMvc.perform(post("/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newBlock))
                )
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(createdBlock)))
                .andExpect(status().isCreated());
        verify(blockService).createBlock(newBlock);
    }

    @Test
    @DisplayName("Should fail to update a block and return a status code 400 if block dates are invalid")
    public void shouldFailToUpdateABlockIfBlockDatesAreInvalid() throws Exception {
        var blockId = 1L;
        Block block = new Block(null, null, new Property(1L));
        mockMvc.perform(put("/blocks/{id}", blockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(block))
                )
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.errors.startDate").value("Start date is required"))
                .andExpect(jsonPath("$.errors.endDate").value("End date is required"))
                .andExpect(status().isBadRequest());
        verify(blockService, never()).updateBlock(block);
    }

    @Test
    @DisplayName("Should fail to update a block and return a status code 422 if not able to update the block")
    public void shouldFailToCreateABlockIfNotAbleToUpdateTheBlock() throws Exception {
        var blockId = 1L;
        LocalDate startDate = LocalDate.of(2024, 4, 10);
        LocalDate endDate = LocalDate.of(2024, 4, 12);
        Block block = new Block(startDate, endDate, new Property(1L));
        block.setId(blockId);
        BusinessException exception = new BusinessException("Couldn't update block with id=" + block.getId(), ErrorCode.UNEXPECTED_ERROR);

        when(blockService.updateBlock(any(Block.class))).thenThrow(exception);

        mockMvc.perform(put("/blocks/{id}", blockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(block))
                )
                .andDo(print())
                .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)))
                .andExpect(jsonPath("$.message").value(exception.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(exception.getErrorCode().name()))
                .andExpect(status().isUnprocessableEntity());
        verify(blockService).updateBlock(block);
    }

    @Test
    @DisplayName("Should delete a block and return a status code 204")
    public void shouldDeleteABlockWithSuccess() throws Exception {
        Long blockId = 1L;

        mockMvc.perform(delete("/blocks/{id}", blockId))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(blockService).deleteBlock(blockId);
    }
}