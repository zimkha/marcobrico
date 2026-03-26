package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Product;
import com.mmd.marcobrico.domain.Sale;
import com.mmd.marcobrico.domain.SaleItem;
import com.mmd.marcobrico.domain.User;
import com.mmd.marcobrico.dto.sale.SaleCreateDto;
import com.mmd.marcobrico.dto.sale.SaleItemDto;
import com.mmd.marcobrico.dto.sale.SaleResponseDto;
import com.mmd.marcobrico.mapper.SaleMapper;
import com.mmd.marcobrico.repository.InventoryRepository;
import com.mmd.marcobrico.repository.ProductRepository;
import com.mmd.marcobrico.repository.SaleRepository;
import com.mmd.marcobrico.repository.UserRepository;
import com.mmd.marcobrico.service.InventoryService;
import com.mmd.marcobrico.service.jwt.AuthenticatedUserService;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {


    @Mock private SaleRepository saleRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private UserRepository userRepository;
    @Mock private InventoryService inventoryService;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @InjectMocks SaleServiceImpl saleService;
    @Captor
    ArgumentCaptor<Sale> saleArgumentCaptor;

    User user;

    @BeforeEach
    void setUp() {
       user = Instancio.create(User.class);
       when(authenticatedUserService.getUserConnected()).thenReturn(user);
    }

    @Nested
    @DisplayName("Should tested created methode")
    class createdTest {

        @Test
        @DisplayName("Should return a new SaleResponseDto without error")
        void shouldCreatedNewSaleResponseDTOWithoutAnyError() {
            // Given
            var product = Instancio.create(Product.class);
            var quantity = 2;
            var price = BigDecimal.valueOf(50);
            var expectedTotal = price.multiply(BigDecimal.valueOf(quantity));

            var itemDto = Instancio.of(SaleItemDto.class)
                    .set(field(SaleItemDto::productId), product.getId())
                    .set(field(SaleItemDto::quantity), 2)
                    .set(field(SaleItemDto::price), BigDecimal.valueOf(50))
                    .create();

            var dto = Instancio.of(SaleCreateDto.class)
                    .set(field(SaleCreateDto::items), List.of(itemDto))
                    .create();

            var persistedSale = Instancio.create(Sale.class);
            var expectedResponse = Instancio.of(SaleResponseDto.class)
                    .set(field(SaleResponseDto::id), persistedSale.getId())
                    .create();

            // When
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            doNothing().when(inventoryService).deductStock(any(), anyInt(), anyString());

            when(saleRepository.save(any(Sale.class))).thenReturn(persistedSale);
            when(saleMapper.toDto(persistedSale)).thenReturn(expectedResponse);

            var response = saleService.createSale(dto);
            // Assert & Verify
            assertEquals(response.id(), expectedResponse.id());
            verify(saleRepository).save(saleArgumentCaptor.capture());
            var capture = saleArgumentCaptor.getValue();
            assertEquals(capture.getUser().getId(), user.getId());

           var capturedItem = capture.getItems().getFirst();
           assertEquals(product.getId(), capturedItem.getProduct().getId());
           assertEquals(expectedTotal, capturedItem.getPrice().multiply(BigDecimal.valueOf(capturedItem.getQuantity())));

           // assertEquals(capture.getCreatedAt(),  expectedResponse.createdAt());
            verify(inventoryService).deductStock(eq(product), eq(2), eq("Vente"));

        }
    }

}