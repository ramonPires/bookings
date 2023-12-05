package com.bookings.service;

import com.bookings.exception.BusinessException;
import com.bookings.exception.ErrorCode;
import com.bookings.exception.PropertyAlreadyBlockedException;
import com.bookings.models.Block;
import com.bookings.repository.BlockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BlockService {
    private final BlockRepository blockRepository;

    public BlockService(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    public Block createBlock(Block block) {
        checkBlockDates(block);
        checkAlreadyBlocked(block);
        return this.blockRepository.save(block);
    }

    public boolean isBlocked(LocalDate blockDate, Long propertyId) {
        return blockRepository.existsByBlockDateAndPropertyId(blockDate, propertyId);
    }

    public void deleteBlock(Long id) {
        blockRepository.deleteById(id);
    }

    public Block updateBlock(Block block) {
        checkBlockDates(block);
        return blockRepository
                .findById(block.getId())
                .map(b -> blockRepository.save(block))
                .orElseThrow(() -> new BusinessException("Couldn't update block with id=" + block.getId(), ErrorCode.UNEXPECTED_ERROR));
    }

    private void checkAlreadyBlocked(Block block) {
        if (this.isBlocked(block.getStartDate(), block.getPropertyId())) {
            throw new PropertyAlreadyBlockedException(block);
        }
    }

    private void checkBlockDates(Block block) {
        if (!block.hasValidBlockDates()) {
            throw new BusinessException("Block dates are invalid", ErrorCode.INVALID_BLOCK_DATES);
        }
    }
}
