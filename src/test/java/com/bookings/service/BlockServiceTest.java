package com.bookings.service;

import com.bookings.exception.BusinessException;
import com.bookings.exception.ErrorCode;
import com.bookings.exception.PropertyAlreadyBlockedException;
import com.bookings.models.Block;
import com.bookings.models.Property;
import com.bookings.repository.BlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BlockServiceTest {
    @Autowired
    BlockService blockService;

    @Autowired
    BlockRepository blockRepository;

    @BeforeEach
    public void cleanDB() {
        blockRepository.deleteAll();
    }


    @ParameterizedTest
    @CsvSource({
            "2024-02-01, 2024-02-01",
            "2024-02-08, 2024-02-07",
            "2025-02-08, 2024-02-07",
    })
    @DisplayName("Should prevent create a block when block dates are not valid")
    public void shouldPreventCreateABlockWhenBlockDatesAreNotValid(ArgumentsAccessor arguments) {
        var startDate = arguments.get(0, LocalDate.class);
        var endDate = arguments.get(1, LocalDate.class);
        Block block = new Block(startDate, endDate, new Property(1L));
        BusinessException exception = assertThrows(BusinessException.class, () -> blockService.createBlock(block));
        assertThat(exception.getErrorCode(), is(ErrorCode.INVALID_BLOCK_DATES));
    }

    @ParameterizedTest
    @CsvSource({
            "2024-02-01, 2024-02-05",
            "2024-02-02, 2024-02-10"
    })
    @DisplayName("Should prevent create a block if a block already exists for the same dates")
    public void shouldPreventCreateABlockIfABlockedAlreadyExistsForTheSameDate(ArgumentsAccessor arguments) {
        var newBlockStartDate = arguments.get(0, LocalDate.class);
        var newBlockEndDate = arguments.get(1, LocalDate.class);
        Block newBlock = new Block(newBlockStartDate, newBlockEndDate, new Property(1L));
        blockService.createBlock(new Block(newBlockStartDate, newBlockEndDate, new Property(1L)));
        PropertyAlreadyBlockedException exception = assertThrowsExactly(PropertyAlreadyBlockedException.class, () -> blockService.createBlock(newBlock), PropertyAlreadyBlockedException.ERROR_MESSAGE_PATTERN.formatted(
                newBlock.getPropertyId(), newBlock.getStartDate(), newBlock.getEndDate()));
        assertThat(exception.getErrorCode(), is(ErrorCode.PROPERTY_ALREADY_BLOCKED));
    }

    @ParameterizedTest
    @CsvSource({
            "2024-02-01, 2024-02-05",
            "2024-02-02, 2024-02-10"
    })
    @DisplayName("Should create a block with success")
    public void shouldCreateABlockWithSuccess(ArgumentsAccessor arguments) {
        var startDate = arguments.get(0, LocalDate.class);
        var endDate = arguments.get(1, LocalDate.class);
        Block block = new Block(startDate, endDate, new Property(1L));
        assertDoesNotThrow(() -> {
            Block persistedBlock = blockService.createBlock(block);
            assertThat(persistedBlock, is(equalTo(block)));
        });
    }

    @Test
    @DisplayName("Should delete a block")
    public void shouldDeleteABlock() {
        var startDate = LocalDate.of(2024, 2, 1);
        var endDate = LocalDate.of(2024, 2, 5);
        Block block = new Block(startDate, endDate, new Property(1L));
        Block persistedBlock = blockService.createBlock(block);
        blockService.deleteBlock(persistedBlock.getId());
        assertThat(blockRepository.findById(persistedBlock.getId()).isPresent(), is(false));
    }

    @Test
    @DisplayName("Should prevent update a block with invalid new invalid dates")
    public void shouldPreventUpdateABlockWithNewInvalidSDates() {
        // Create a new block
        var startDate = LocalDate.of(2024, 2, 1);
        var endDate = LocalDate.of(2024, 2, 5);
        Block block = new Block(startDate, endDate, new Property(1L));
        Block persistedBlock = blockService.createBlock(block);

        // Try to update a block
        var updatedStartDate = startDate.plusDays(1);
        var updatedEndDate = startDate.minusDays(1);
        Block updatedBlock = new Block(updatedStartDate, updatedEndDate, new Property(1L));
        updatedBlock.setId(persistedBlock.getId());

        BusinessException exception = assertThrows(BusinessException.class, () -> blockService.updateBlock(updatedBlock));
        assertThat(exception.getErrorCode(), is(ErrorCode.INVALID_BLOCK_DATES));
    }

    @Test
    @DisplayName("Should prevent update a block if fail to update")
    public void shouldPreventUpdateABlockIfFailToUpdate() {
        // Create a new block
        var startDate = LocalDate.of(2024, 2, 1);
        var endDate = LocalDate.of(2024, 2, 5);
        Block block = new Block(startDate, endDate, new Property(1L));
        Block persistedBlock = blockService.createBlock(block);

        var updatedStartDate = startDate.plusDays(1);
        var updatedEndDate = updatedStartDate.plusDays(5);
        Block updatedBlock = new Block(updatedStartDate, updatedEndDate, new Property(1L));
        updatedBlock.setId(persistedBlock.getId());

        // Create an unexpected error
        blockService.deleteBlock(persistedBlock.getId());

        // Try to update a block
        BusinessException exception = assertThrows(BusinessException.class, () -> blockService.updateBlock(updatedBlock));
        assertThat(exception.getErrorCode(), is(ErrorCode.UNEXPECTED_ERROR));
    }

    @Test
    @DisplayName("Should update a block with success")
    public void shouldUpdateABlockWithSuccess() {
        // Create a new block
        var startDate = LocalDate.of(2024, 2, 1);
        var endDate = LocalDate.of(2024, 2, 5);
        Block block = new Block(startDate, endDate, new Property(1L));
        Block persistedBlock = blockService.createBlock(block);

        var updatedStartDate = startDate.plusDays(1);
        var updatedEndDate = updatedStartDate.plusDays(5);
        Block updatedBlock = new Block(updatedStartDate, updatedEndDate, new Property(1L));
        updatedBlock.setId(persistedBlock.getId());

        // Try to update a block
        assertDoesNotThrow(() -> {
            Block updated = blockService.updateBlock(updatedBlock);
            assertThat(updated, is(equalTo(updatedBlock)));
        });
    }
}