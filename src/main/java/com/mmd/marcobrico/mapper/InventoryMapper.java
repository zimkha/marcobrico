package com.mmd.marcobrico.mapper;
import com.mmd.marcobrico.domain.InventoryEntry;
import com.mmd.marcobrico.dto.inventory.InventoryResponseDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InventoryMapper {
    default InventoryResponseDto toDto(InventoryEntry entry) {
        return new InventoryResponseDto(
                entry.getId(),
                entry.getProduct().getId(),
                entry.getProduct().getName(),
                entry.getQuantityBefore(),
                entry.getQuantityAfter(),
                entry.getType().name(),
                entry.getComment(),
                entry.getCreatedAt(),
                entry.getUser().getUsername()

        );
    }
}
