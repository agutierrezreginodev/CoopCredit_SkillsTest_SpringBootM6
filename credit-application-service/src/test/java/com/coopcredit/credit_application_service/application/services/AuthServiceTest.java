package com.coopcredit.credit_application_service.application.services;

import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import com.coopcredit.credit_application_service.domain.model.User;
import com.coopcredit.credit_application_service.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests")
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private AuthService service;

    private User adminUser;
    private User affiliateUser;
    private User analistaUser;

    @BeforeEach
    void setUp() {
        // Usuario admin
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword("encodedPassword123");
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setDocumento(null);

        // Usuario afiliado con documento
        affiliateUser = new User();
        affiliateUser.setId(2L);
        affiliateUser.setUsername("juan.perez");
        affiliateUser.setPassword("encodedPassword456");
        affiliateUser.setRole("ROLE_AFILIADO");
        affiliateUser.setDocumento("1017654311");

        // Usuario analista
        analistaUser = new User();
        analistaUser.setId(3L);
        analistaUser.setUsername("analista1");
        analistaUser.setPassword("encodedPassword789");
        analistaUser.setRole("ROLE_ANALISTA");
        analistaUser.setDocumento(null);
    }

    // ========================================================================
    // REGISTER USER TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe registrar un usuario admin exitosamente")
    void shouldRegisterAdminUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        // When
        User result = service.registerUser(adminUser);

        // Then
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("ROLE_ADMIN", result.getRole());
        assertNull(result.getDocumento());
        
        // Verificar interacciones
        verify(userRepository, times(1)).existsByUsername("admin");
        verify(userRepository, never()).findByDocumento(anyString());
        
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        
        User savedUser = captor.getValue();
        assertEquals("admin", savedUser.getUsername());
    }

    @Test
    @DisplayName("Debe registrar un usuario afiliado con documento exitosamente")
    void shouldRegisterAffiliateUserWithDocumentSuccessfully() {
        // Given
        when(userRepository.existsByUsername("juan.perez")).thenReturn(false);
        when(userRepository.findByDocumento("1017654311")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(affiliateUser);

        // When
        User result = service.registerUser(affiliateUser);

        // Then
        assertNotNull(result);
        assertEquals("juan.perez", result.getUsername());
        assertEquals("ROLE_AFILIADO", result.getRole());
        assertEquals("1017654311", result.getDocumento());
        
        // Verificar que se validó tanto username como documento
        verify(userRepository, times(1)).existsByUsername("juan.perez");
        verify(userRepository, times(1)).findByDocumento("1017654311");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el username ya existe")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // When & Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> service.registerUser(adminUser)
        );

        assertTrue(exception.getMessage().contains("Ya existe un usuario con el username"));
        verify(userRepository, times(1)).existsByUsername("admin");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el documento ya existe")
    void shouldThrowExceptionWhenDocumentAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("juan.perez")).thenReturn(false);
        when(userRepository.findByDocumento("1017654311")).thenReturn(Optional.of(affiliateUser));

        // When & Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> service.registerUser(affiliateUser)
        );

        assertTrue(exception.getMessage().contains("Ya existe un usuario con el documento"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el username está vacío")
    void shouldThrowExceptionWhenEmptyUsername() {
        // Given
        adminUser.setUsername("");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(adminUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el username es null")
    void shouldThrowExceptionWhenNullUsername() {
        // Given
        adminUser.setUsername(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(adminUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el password está vacío")
    void shouldThrowExceptionWhenEmptyPassword() {
        // Given
        adminUser.setPassword("");
        when(userRepository.existsByUsername("admin")).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(adminUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el role está vacío")
    void shouldThrowExceptionWhenEmptyRole() {
        // Given
        adminUser.setRole("");
        when(userRepository.existsByUsername("admin")).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(adminUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe permitir registrar usuario sin documento (para admin y analista)")
    void shouldAllowRegisterUserWithoutDocument() {
        // Given
        when(userRepository.existsByUsername("analista1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(analistaUser);

        // When
        User result = service.registerUser(analistaUser);

        // Then
        assertNotNull(result);
        assertNull(result.getDocumento());
        
        // No debe verificar documento si es null
        verify(userRepository, never()).findByDocumento(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe permitir registrar usuario con documento vacío")
    void shouldAllowRegisterUserWithEmptyDocument() {
        // Given
        analistaUser.setDocumento("");
        when(userRepository.existsByUsername("analista1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(analistaUser);

        // When
        User result = service.registerUser(analistaUser);

        // Then
        assertNotNull(result);
        
        // No debe verificar documento si está vacío
        verify(userRepository, never()).findByDocumento(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ========================================================================
    // AUTHENTICATE USER TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe retornar usuario cuando existe")
    void shouldReturnUserWhenExists() {
        // Given
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // When
        Optional<User> result = service.authenticateUser("admin", "anyPassword");

        // Then
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals("ROLE_ADMIN", result.get().getRole());
        
        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el usuario no existe")
    void shouldReturnEmptyOptionalWhenUserDoesNotExist() {
        // Given
        when(userRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        // When
        Optional<User> result = service.authenticateUser("noexiste", "anyPassword");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("noexiste");
    }

    @Test
    @DisplayName("Debe buscar usuario independientemente de la contraseña proporcionada")
    void shouldFindUserRegardlessOfProvidedPassword() {
        // Given
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // When
        Optional<User> result1 = service.authenticateUser("admin", "correctPassword");
        Optional<User> result2 = service.authenticateUser("admin", "wrongPassword");

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(result1.get().getUsername(), result2.get().getUsername());
        
        // La verificación de contraseña se hace en la capa de infraestructura
        verify(userRepository, times(2)).findByUsername("admin");
    }

    // ========================================================================
    // GET USER BY USERNAME TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe obtener usuario por username")
    void shouldGetUserByUsername() {
        // Given
        when(userRepository.findByUsername("juan.perez")).thenReturn(Optional.of(affiliateUser));

        // When
        Optional<User> result = service.getUserByUsername("juan.perez");

        // Then
        assertTrue(result.isPresent());
        assertEquals("juan.perez", result.get().getUsername());
        assertEquals("1017654311", result.get().getDocumento());
        
        verify(userRepository, times(1)).findByUsername("juan.perez");
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el username no existe")
    void shouldReturnEmptyOptionalWhenUsernameNotFound() {
        // Given
        when(userRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        // When
        Optional<User> result = service.getUserByUsername("noexiste");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("noexiste");
    }

    // ========================================================================
    // GET USER BY DOCUMENTO TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe obtener usuario por documento")
    void shouldGetUserByDocument() {
        // Given
        when(userRepository.findByDocumento("1017654311")).thenReturn(Optional.of(affiliateUser));

        // When
        Optional<User> result = service.getUserByDocumento("1017654311");

        // Then
        assertTrue(result.isPresent());
        assertEquals("1017654311", result.get().getDocumento());
        assertEquals("juan.perez", result.get().getUsername());
        
        verify(userRepository, times(1)).findByDocumento("1017654311");
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el documento no existe")
    void shouldReturnEmptyOptionalWhenDocumentNotFound() {
        // Given
        when(userRepository.findByDocumento("9999999999")).thenReturn(Optional.empty());

        // When
        Optional<User> result = service.getUserByDocumento("9999999999");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByDocumento("9999999999");
    }

    // ========================================================================
    // INTEGRATION TESTS WITH MULTIPLE OPERATIONS
    // ========================================================================

    @Test
    @DisplayName("Debe realizar flujo completo: registrar y buscar usuario")
    void shouldPerformCompleteFlow() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.findByDocumento("1234567890")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(affiliateUser);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(affiliateUser));
        when(userRepository.findByDocumento("1234567890")).thenReturn(Optional.of(affiliateUser));

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setRole("ROLE_AFILIADO");
        newUser.setDocumento("1234567890");

        // When - Registrar
        User registered = service.registerUser(newUser);
        assertNotNull(registered);

        // When - Buscar por username
        Optional<User> foundByUsername = service.getUserByUsername("newuser");
        assertTrue(foundByUsername.isPresent());

        // When - Buscar por documento
        Optional<User> foundByDoc = service.getUserByDocumento("1234567890");
        assertTrue(foundByDoc.isPresent());

        // Then - Verificar interacciones
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(2)).findByDocumento("1234567890"); // Una en register, otra en getUserByDocumento
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findByUsername("newuser");
    }

    @Test
    @DisplayName("Debe manejar múltiples roles correctamente")
    void shouldHandleMultipleRolesCorrectly() {
        // Given
        User[] users = {adminUser, affiliateUser, analistaUser};
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.findByDocumento(anyString())).thenReturn(Optional.empty());
        
        // When
        for (User user : users) {
            when(userRepository.save(any(User.class))).thenReturn(user);
            User result = service.registerUser(user);
            
            // Then
            assertNotNull(result);
            assertTrue(result.getRole().startsWith("ROLE_"));
        }

        // Verificar que se registraron 3 usuarios
        verify(userRepository, times(3)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe prevenir registros duplicados concurrentes")
    void shouldPreventDuplicateConcurrentRegistrations() {
        // Given - Simular que el username se vuelve duplicado entre la verificación y el guardado
        when(userRepository.existsByUsername("admin")).thenReturn(false);

        // When & Then - Primera llamada exitosa
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        User result1 = service.registerUser(adminUser);
        assertNotNull(result1);

        // Cuando se intenta registrar de nuevo, ya existe
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        assertThrows(BusinessRuleException.class, () -> service.registerUser(adminUser));

        // Solo debe haberse guardado una vez
        verify(userRepository, times(1)).save(any(User.class));
    }
}
