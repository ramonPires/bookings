package com.bookings.controller;

import com.bookings.models.Block;
import com.bookings.service.BlockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blocks")
public class BlockController {
    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping
    public ResponseEntity<Block> createBlock(@Valid @RequestBody Block block) {
        return new ResponseEntity<>(this.blockService.createBlock(block), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Block> updateBlock(@Valid @RequestBody Block block, @PathVariable("id") Long id) {
        block.setId(id);
        Block updatedBlock = this.blockService.updateBlock(block);
        return ResponseEntity.ok(updatedBlock);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlock(@PathVariable("id") Long id) {
        this.blockService.deleteBlock(id);
    }

}
