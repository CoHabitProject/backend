package fr.esgi.rest.interne;

import fr.esgi.domain.dto.space.ColocationReqDto;
import fr.esgi.domain.dto.space.ColocationResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.space.ColocationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColocationRestTest {

    @Mock
    private ColocationService colocationService;

    @InjectMocks
    private ColocationRest colocationRest;

    private ColocationReqDto testRequestDto;
    private ColocationResDto testResponseDto;
    private Long             testColocationId;
    private String           testInvitationCode;

    @BeforeEach
    void setUp() {
        testColocationId   = 1L;
        testInvitationCode = "ABC123";
        testRequestDto     = createTestRequestDto();
        testResponseDto    = createTestResponseDto();
    }

    @Test
    void createColocation_ShouldReturnCreatedColocation() throws
                                                          TechnicalException {
        // Given
        when(colocationService.createColocation(any(ColocationReqDto.class)))
                .thenReturn(testResponseDto);

        // When
        ResponseEntity<ColocationResDto> result = colocationRest.createColocation(testRequestDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(testResponseDto);
        verify(colocationService).createColocation(testRequestDto);
    }

    @Test
    void updateColocation_ShouldReturnUpdatedColocation() throws
                                                          TechnicalException {
        // Given
        when(colocationService.updateColocation(eq(testColocationId), any(ColocationReqDto.class)))
                .thenReturn(testResponseDto);

        // When
        ResponseEntity<ColocationResDto> result = colocationRest.updateColocation(testColocationId, testRequestDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(testResponseDto);
        verify(colocationService).updateColocation(testColocationId, testRequestDto);
    }

    @Test
    void getColocationById_ShouldReturnColocation() throws
                                                    TechnicalException {
        // Given
        when(colocationService.getColocationById(testColocationId))
                .thenReturn(testResponseDto);

        // When
        ResponseEntity<ColocationResDto> result = colocationRest.getColocationById(testColocationId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(testResponseDto);
        verify(colocationService).getColocationById(testColocationId);
    }

    @Test
    void getManagedColocations_ShouldReturnColocationList() throws
                                                            TechnicalException {
        // Given
        List<ColocationResDto> expectedColocations = List.of(testResponseDto);
        when(colocationService.getManagedColocations()).thenReturn(expectedColocations);

        // When
        ResponseEntity<List<ColocationResDto>> result = colocationRest.getManagedColocations();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedColocations);
        assertThat(result.getBody()).hasSize(1);
        verify(colocationService).getManagedColocations();
    }

    @Test
    void getUserColocations_ShouldReturnColocationList() throws
                                                         TechnicalException {
        // Given
        List<ColocationResDto> expectedColocations = List.of(testResponseDto);
        when(colocationService.getUserColocations()).thenReturn(expectedColocations);

        // When
        ResponseEntity<List<ColocationResDto>> result = colocationRest.getUserColocations();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedColocations);
        assertThat(result.getBody()).hasSize(1);
        verify(colocationService).getUserColocations();
    }

    @Test
    void deleteColocation_ShouldCallServiceAndReturnNoContent() throws
                                                                TechnicalException {
        // When
        ResponseEntity<Void> result = colocationRest.deleteColocation(testColocationId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        verify(colocationService).deleteColocation(testColocationId);
    }

    @Test
    void joinColocation_ShouldReturnJoinedColocation() throws
                                                       TechnicalException {
        // Given
        when(colocationService.joinColocation(testInvitationCode))
                .thenReturn(testResponseDto);

        // When
        ResponseEntity<ColocationResDto> result = colocationRest.joinColocation(testInvitationCode);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(testResponseDto);
        verify(colocationService).joinColocation(testInvitationCode);
    }

    @Test
    void leaveColocation_ShouldCallServiceAndReturnNoContent() throws
                                                               TechnicalException {
        // When
        ResponseEntity<Void> result = colocationRest.leaveColocation(testColocationId);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        verify(colocationService).leaveColocation(testColocationId);
    }

    // Test data builders
    private ColocationReqDto createTestRequestDto() {
        return new ColocationReqDto("Test Colocation", "Paris", "123 Test Street", "75001");
    }

    private ColocationResDto createTestResponseDto() {
        return new ColocationResDto(1L, "Test Colocation", "Paris", "123 Test Street", "75001", 5);
    }
}
