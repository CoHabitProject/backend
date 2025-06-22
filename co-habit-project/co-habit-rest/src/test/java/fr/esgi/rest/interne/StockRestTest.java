package fr.esgi.rest.interne;

import fr.esgi.domain.dto.space.StockItemReqDto;
import fr.esgi.domain.dto.space.StockItemResDto;
import fr.esgi.domain.dto.space.StockReqDto;
import fr.esgi.domain.dto.space.StockResDto;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.space.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockRestTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockRest stockRest;

    private Long            colocationId;
    private Long            stockId;
    private Long            itemId;
    private StockReqDto     testStockRequestDto;
    private StockResDto     testStockResponseDto;
    private StockItemReqDto testItemRequestDto;
    private StockItemResDto testItemResponseDto;

    @BeforeEach
    void setUp() {
        colocationId         = 1L;
        stockId              = 2L;
        itemId               = 3L;
        testStockRequestDto  = createTestStockRequestDto();
        testStockResponseDto = createTestStockResponseDto();
        testItemRequestDto   = createTestItemRequestDto();
        testItemResponseDto  = createTestItemResponseDto();
    }

    // Stock endpoints tests

    @Test
    void createStock_ShouldReturnCreatedStock() throws
                                                TechnicalException {
        // Given
        when(stockService.createStock(eq(colocationId), any(StockReqDto.class)))
                .thenReturn(testStockResponseDto);

        // When
        ResponseEntity<StockResDto> result = stockRest.createStock(colocationId, testStockRequestDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(testStockResponseDto);
        verify(stockService).createStock(colocationId, testStockRequestDto);
    }

    @Test
    void updateStock_ShouldReturnUpdatedStock() throws
                                                TechnicalException {
        // Given
        when(stockService.updateStock(eq(colocationId), eq(stockId), any(StockReqDto.class)))
                .thenReturn(testStockResponseDto);

        // When
        ResponseEntity<StockResDto> result = stockRest.updateStock(colocationId, stockId, testStockRequestDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(testStockResponseDto);
        verify(stockService).updateStock(colocationId, stockId, testStockRequestDto);
    }

    @Test
    void getStocksByColocation_ShouldReturnStockList() throws
                                                       TechnicalException {
        // Given
        List<StockResDto> expectedStocks = List.of(testStockResponseDto);
        when(stockService.getStocksByColocation(colocationId)).thenReturn(expectedStocks);

        // When
        ResponseEntity<List<StockResDto>> result = stockRest.getStocksByColocation(colocationId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedStocks);
        assertThat(result.getBody()).hasSize(1);
        verify(stockService).getStocksByColocation(colocationId);
    }

    @Test
    void getStockById_ShouldReturnStock() throws
                                          TechnicalException {
        // Given
        when(stockService.getStockById(colocationId, stockId)).thenReturn(testStockResponseDto);

        // When
        ResponseEntity<StockResDto> result = stockRest.getStockById(colocationId, stockId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(testStockResponseDto);
        verify(stockService).getStockById(colocationId, stockId);
    }

    @Test
    void deleteStock_ShouldCallServiceAndReturnNoContent() throws
                                                           TechnicalException {
        // When
        ResponseEntity<Void> result = stockRest.deleteStock(colocationId, stockId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        verify(stockService).deleteStock(colocationId, stockId);
    }

    // Stock items endpoints tests

    @Test
    void addItemToStock_ShouldReturnCreatedItem() throws
                                                  TechnicalException {
        // Given
        when(stockService.addItemToStock(eq(colocationId), eq(stockId), any(StockItemReqDto.class)))
                .thenReturn(testItemResponseDto);

        // When
        ResponseEntity<StockItemResDto> result = stockRest.addItemToStock(colocationId, stockId, testItemRequestDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(testItemResponseDto);
        verify(stockService).addItemToStock(colocationId, stockId, testItemRequestDto);
    }

    @Test
    void updateStockItem_ShouldReturnUpdatedItem() throws
                                                   TechnicalException {
        // Given
        when(stockService.updateStockItem(eq(colocationId), eq(stockId), eq(itemId), any(StockItemReqDto.class)))
                .thenReturn(testItemResponseDto);

        // When
        ResponseEntity<StockItemResDto> result = stockRest.updateStockItem(colocationId, stockId, itemId, testItemRequestDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(testItemResponseDto);
        verify(stockService).updateStockItem(colocationId, stockId, itemId, testItemRequestDto);
    }

    @Test
    void getStockItems_ShouldReturnItemList() throws
                                              TechnicalException {
        // Given
        List<StockItemResDto> expectedItems = List.of(testItemResponseDto);
        when(stockService.getStockItems(colocationId, stockId)).thenReturn(expectedItems);

        // When
        ResponseEntity<List<StockItemResDto>> result = stockRest.getStockItems(colocationId, stockId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedItems);
        assertThat(result.getBody()).hasSize(1);
        verify(stockService).getStockItems(colocationId, stockId);
    }

    @Test
    void deleteStockItem_ShouldCallServiceAndReturnNoContent() throws
                                                               TechnicalException {
        // When
        ResponseEntity<Void> result = stockRest.deleteStockItem(colocationId, stockId, itemId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        verify(stockService).deleteStockItem(colocationId, stockId, itemId);
    }

    @Test
    void addItemToStock_ShouldThrowExceptionWhenCapacityExceeded() throws
                                                                   TechnicalException {
        // Given
        when(stockService.addItemToStock(eq(colocationId), eq(stockId), any(StockItemReqDto.class)))
                .thenThrow(new TechnicalException(400, "La quantité totale (15) dépasserait la capacité maximale du stock (10)"));

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockRest.addItemToStock(colocationId, stockId, testItemRequestDto));

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage()
                            .contains("dépasserait la capacité maximale du stock"));
    }

    @Test
    void updateStockItem_ShouldThrowExceptionWhenCapacityExceeded() throws
                                                                    TechnicalException {
        // Given
        when(stockService.updateStockItem(eq(colocationId), eq(stockId), eq(itemId), any(StockItemReqDto.class)))
                .thenThrow(new TechnicalException(400, "La quantité totale (20) dépasserait la capacité maximale du stock (15)"));

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockRest.updateStockItem(colocationId, stockId, itemId, testItemRequestDto));

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage()
                            .contains("dépasserait la capacité maximale du stock"));
    }

    // Test data builders
    private StockReqDto createTestStockRequestDto() {
        return StockReqDto.builder()
                          .title("Test Stock")
                          .imageAsset("test-stock.png")
                          .color("#FF5733")
                          .maxCapacity(50)
                          .build();
    }

    private StockResDto createTestStockResponseDto() {
        return StockResDto.builder()
                          .id(stockId)
                          .title("Test Stock")
                          .imageAsset("test-stock.png")
                          .color("#FF5733")
                          .maxCapacity(50)
                          .itemCount(5)
                          .build();
    }

    private StockItemReqDto createTestItemRequestDto() {
        return StockItemReqDto.builder()
                              .name("Test Item")
                              .quantity(10)
                              .build();
    }

    private StockItemResDto createTestItemResponseDto() {
        UserProfileResDto addedByUser = UserProfileResDto.builder()
                                                         .id(1L)
                                                         .firstName("John")
                                                         .lastName("Doe")
                                                         .fullName("John Doe")
                                                         .email("john.doe@example.com")
                                                         .build();

        return StockItemResDto.builder()
                              .id(itemId)
                              .name("Test Item")
                              .quantity(10)
                              .addedBy(addedByUser)
                              .build();
    }
}
