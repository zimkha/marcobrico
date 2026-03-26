package com.mmd.marcobrico.service.impl;

import com.mmd.marcobrico.domain.Client;
import com.mmd.marcobrico.dto.client.ClientCreateDto;
import com.mmd.marcobrico.dto.client.ClientResponseDto;
import com.mmd.marcobrico.mapper.ClientMapper;
import com.mmd.marcobrico.repository.ClientRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("ClientServiceImplTest - Tests unitaires")
class ClientServiceImplTest {
    @Mock
    private  ClientRepository repository;
    @Mock
    private  ClientMapper mapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Captor
    private ArgumentCaptor<Client> clientArgumentCaptor;

    @Nested
    @DisplayName("Tests de la méthode create()")
    class CreatedTest {
        @Test
        @DisplayName("Devrait créer un client avec succès")
        void shouldCreateClientSuccessfully() {
            // Giver
            ClientCreateDto dto = Instancio.create(ClientCreateDto.class);
            Client client = Instancio.create(Client.class);
            Client savedClient = Instancio.create(Client.class);
            ClientResponseDto expectedResp = Instancio.create(ClientResponseDto.class);

            when(repository.save(any(Client.class))).thenReturn(savedClient);
            when(mapper.toDto(savedClient)).thenReturn(expectedResp);
            // WHEN
            ClientResponseDto actual = clientService.create(dto);
            // THEN
            assertNotNull(actual);
            assertEquals(expectedResp, actual);
            verify(repository, times(1)).save(any(Client.class));
            verify(repository).save(clientArgumentCaptor.capture());
            Client captured = clientArgumentCaptor.getValue();
            verify(mapper,     times(1)).toDto(savedClient);
            assertEquals(dto.name(), captured.getName());
            assertEquals(dto.email(), captured.getEmail());

        }

        @Test
        void shouldThrowExceptionWhenDtoIsNull(){

        }
        @Test
        void shouldThrowExceptionWhenClientInvalid() {

        }

        @Test
        void shouldThrowExceptionWhenClientAlreadyExists() {

        }
        @Test
        void shouldCallRepositoryOnce() {

        }
    }

}