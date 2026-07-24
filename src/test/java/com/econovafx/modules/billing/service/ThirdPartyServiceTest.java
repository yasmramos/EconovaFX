package com.econovafx.modules.billing.service;

import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.billing.repository.ThirdPartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThirdPartyServiceTest {

    @Mock
    private ThirdPartyRepository thirdPartyRepository;

    private ThirdPartyService thirdPartyService;

    @BeforeEach
    void setUp() {
        thirdPartyService = new ThirdPartyService(thirdPartyRepository);
    }

    @Test
    void testGetThirdPartyById_WhenExists_ReturnsOptional() {
        Long id = 1L;
        ThirdParty thirdParty = createThirdParty(id, "Test Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.of(thirdParty));
        
        Optional<ThirdParty> result = thirdPartyService.getThirdPartyById(id);
        
        assertTrue(result.isPresent());
        assertEquals("Test Customer", result.get().getName());
        verify(thirdPartyRepository).findById(id);
    }

    @Test
    void testGetThirdPartyById_WhenNotExists_ReturnsEmpty() {
        Long id = 999L;
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.empty());
        
        Optional<ThirdParty> result = thirdPartyService.getThirdPartyById(id);
        
        assertFalse(result.isPresent());
        verify(thirdPartyRepository).findById(id);
    }

    @Test
    void testGetThirdPartyByIdentification_WhenExists_ReturnsOptional() {
        String identification = "12345678901";
        ThirdParty thirdParty = createThirdParty(1L, "Test Customer", identification, ThirdParty.ThirdPartyType.CUSTOMER);
        
        when(thirdPartyRepository.findByIdentificationNumber(identification)).thenReturn(Optional.of(thirdParty));
        
        Optional<ThirdParty> result = thirdPartyService.getThirdPartyByIdentification(identification);
        
        assertTrue(result.isPresent());
        assertEquals(identification, result.get().getIdentificationNumber());
        verify(thirdPartyRepository).findByIdentificationNumber(identification);
    }

    @Test
    void testGetAllThirdParties_ReturnsList() {
        List<ThirdParty> thirdParties = Arrays.asList(
            createThirdParty(1L, "Customer 1", "11111111111", ThirdParty.ThirdPartyType.CUSTOMER),
            createThirdParty(2L, "Supplier 1", "22222222222", ThirdParty.ThirdPartyType.SUPPLIER)
        );
        
        when(thirdPartyRepository.findAll()).thenReturn(thirdParties);
        
        List<ThirdParty> result = thirdPartyService.getAllThirdParties();
        
        assertEquals(2, result.size());
        verify(thirdPartyRepository).findAll();
    }

    @Test
    void testGetThirdPartiesByType_ReturnsFilteredList() {
        List<ThirdParty> customers = Arrays.asList(
            createThirdParty(1L, "Customer 1", "11111111111", ThirdParty.ThirdPartyType.CUSTOMER),
            createThirdParty(2L, "Customer 2", "22222222222", ThirdParty.ThirdPartyType.CUSTOMER)
        );
        
        when(thirdPartyRepository.findByType(ThirdParty.ThirdPartyType.CUSTOMER)).thenReturn(customers);
        
        List<ThirdParty> result = thirdPartyService.getThirdPartiesByType(ThirdParty.ThirdPartyType.CUSTOMER);
        
        assertEquals(2, result.size());
        verify(thirdPartyRepository).findByType(ThirdParty.ThirdPartyType.CUSTOMER);
    }

    @Test
    void testGetCustomers_ReturnsCustomersAndBoth() {
        List<ThirdParty> customers = Arrays.asList(
            createThirdParty(1L, "Customer 1", "11111111111", ThirdParty.ThirdPartyType.CUSTOMER)
        );
        
        when(thirdPartyRepository.findCustomers()).thenReturn(customers);
        
        List<ThirdParty> result = thirdPartyService.getCustomers();
        
        assertEquals(1, result.size());
        verify(thirdPartyRepository).findCustomers();
    }

    @Test
    void testGetSuppliers_ReturnsSuppliersAndBoth() {
        List<ThirdParty> suppliers = Arrays.asList(
            createThirdParty(1L, "Supplier 1", "11111111111", ThirdParty.ThirdPartyType.SUPPLIER)
        );
        
        when(thirdPartyRepository.findSuppliers()).thenReturn(suppliers);
        
        List<ThirdParty> result = thirdPartyService.getSuppliers();
        
        assertEquals(1, result.size());
        verify(thirdPartyRepository).findSuppliers();
    }

    @Test
    void testGetActiveThirdParties_ReturnsActiveList() {
        List<ThirdParty> activeThirdParties = Arrays.asList(
            createThirdParty(1L, "Active Customer", "11111111111", ThirdParty.ThirdPartyType.CUSTOMER)
        );
        
        when(thirdPartyRepository.findActiveThirdParties()).thenReturn(activeThirdParties);
        
        List<ThirdParty> result = thirdPartyService.getActiveThirdParties();
        
        assertEquals(1, result.size());
        verify(thirdPartyRepository).findActiveThirdParties();
    }

    @Test
    void testSearchThirdParties_ReturnsMatchingResults() {
        String searchTerm = "Test";
        List<ThirdParty> results = Arrays.asList(
            createThirdParty(1L, "Test Customer", "11111111111", ThirdParty.ThirdPartyType.CUSTOMER)
        );
        
        when(thirdPartyRepository.searchByName(searchTerm)).thenReturn(results);
        
        List<ThirdParty> result = thirdPartyService.searchThirdParties(searchTerm);
        
        assertEquals(1, result.size());
        verify(thirdPartyRepository).searchByName(searchTerm);
    }

    @Test
    void testSearchByName_ReturnsMatchingResults() {
        String searchTerm = "John";
        List<ThirdParty> results = Arrays.asList(
            createThirdParty(1L, "John Doe", "11111111111", ThirdParty.ThirdPartyType.CUSTOMER)
        );
        
        when(thirdPartyRepository.searchByName(searchTerm)).thenReturn(results);
        
        List<ThirdParty> result = thirdPartyService.searchByName(searchTerm);
        
        assertEquals(1, result.size());
        verify(thirdPartyRepository).searchByName(searchTerm);
    }

    @Test
    void testSearchByIdentification_ReturnsMatchingResults() {
        String searchTerm = "123";
        List<ThirdParty> results = Arrays.asList(
            createThirdParty(1L, "Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER)
        );
        
        when(thirdPartyRepository.searchByIdentification(searchTerm)).thenReturn(results);
        
        List<ThirdParty> result = thirdPartyService.searchByIdentification(searchTerm);
        
        assertEquals(1, result.size());
        verify(thirdPartyRepository).searchByIdentification(searchTerm);
    }

    @Test
    void testCreateThirdParty_WithValidData_Success() {
        ThirdParty thirdParty = createThirdParty(null, "New Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        ThirdParty savedThirdParty = createThirdParty(1L, "New Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        
        when(thirdPartyRepository.existsByIdentificationNumber("12345678901")).thenReturn(false);
        when(thirdPartyRepository.save(any(ThirdParty.class))).thenReturn(savedThirdParty);
        
        ThirdParty result = thirdPartyService.createThirdParty(thirdParty);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(thirdPartyRepository).save(any(ThirdParty.class));
    }

    @Test
    void testCreateThirdParty_WithNullName_ThrowsException() {
        ThirdParty thirdParty = createThirdParty(null, null, "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            thirdPartyService.createThirdParty(thirdParty);
        });
        
        assertEquals("Name is required", exception.getMessage());
        verify(thirdPartyRepository, never()).save(any());
    }

    @Test
    void testCreateThirdParty_WithEmptyName_ThrowsException() {
        ThirdParty thirdParty = createThirdParty(null, "   ", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            thirdPartyService.createThirdParty(thirdParty);
        });
        
        assertEquals("Name is required", exception.getMessage());
        verify(thirdPartyRepository, never()).save(any());
    }

    @Test
    void testCreateThirdParty_WithNullEmail_ThrowsException() {
        ThirdParty thirdParty = createThirdParty(null, "Test Customer", null, ThirdParty.ThirdPartyType.CUSTOMER);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            thirdPartyService.createThirdParty(thirdParty);
        });
        
        assertEquals("Email is required", exception.getMessage());
        verify(thirdPartyRepository, never()).save(any());
    }

    @Test
    void testCreateThirdParty_WithNullType_ThrowsException() {
        ThirdParty thirdParty = new ThirdParty();
        thirdParty.setName("Test Customer");
        thirdParty.setEmail("test@example.com");
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            thirdPartyService.createThirdParty(thirdParty);
        });
        
        assertEquals("Type is required", exception.getMessage());
        verify(thirdPartyRepository, never()).save(any());
    }

    @Test
    void testCreateThirdParty_WithDuplicateIdentification_ThrowsException() {
        ThirdParty thirdParty = createThirdParty(null, "Test Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        
        when(thirdPartyRepository.existsByIdentificationNumber("12345678901")).thenReturn(true);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            thirdPartyService.createThirdParty(thirdParty);
        });
        
        assertTrue(exception.getMessage().contains("Identification number already exists"));
        verify(thirdPartyRepository, never()).save(any());
    }

    @Test
    void testUpdateThirdParty_WithValidData_Success() {
        ThirdParty thirdParty = createThirdParty(1L, "Updated Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        
        when(thirdPartyRepository.existsById(1L)).thenReturn(true);
        doNothing().when(thirdPartyRepository).update(thirdParty);
        
        ThirdParty result = thirdPartyService.updateThirdParty(thirdParty);
        
        assertEquals("Updated Customer", result.getName());
        verify(thirdPartyRepository).update(thirdParty);
    }

    @Test
    void testUpdateThirdParty_WithNonExistentId_ThrowsException() {
        ThirdParty thirdParty = createThirdParty(999L, "Updated Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        
        when(thirdPartyRepository.existsById(999L)).thenReturn(false);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            thirdPartyService.updateThirdParty(thirdParty);
        });
        
        assertTrue(exception.getMessage().contains("ThirdParty not found with ID"));
        verify(thirdPartyRepository, never()).update(any());
    }

    @Test
    void testDeleteThirdParty_WhenExists_Success() {
        Long id = 1L;
        ThirdParty thirdParty = createThirdParty(id, "To Delete", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.of(thirdParty));
        doNothing().when(thirdPartyRepository).delete(thirdParty);
        
        thirdPartyService.deleteThirdParty(id);
        
        verify(thirdPartyRepository).delete(thirdParty);
    }

    @Test
    void testDeleteThirdParty_WhenNotExists_ThrowsException() {
        Long id = 999L;
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            thirdPartyService.deleteThirdParty(id);
        });
        
        assertTrue(exception.getMessage().contains("ThirdParty not found with ID"));
        verify(thirdPartyRepository, never()).delete(any());
    }

    @Test
    void testDeactivateThirdParty_WhenExists_Success() {
        Long id = 1L;
        ThirdParty thirdParty = createThirdParty(id, "To Deactivate", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        thirdParty.setIsActive(true);
        
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.of(thirdParty));
        doNothing().when(thirdPartyRepository).update(thirdParty);
        
        thirdPartyService.deactivateThirdParty(id);
        
        assertFalse(thirdParty.getIsActive());
        verify(thirdPartyRepository).update(thirdParty);
    }

    @Test
    void testActivateThirdParty_WhenExists_Success() {
        Long id = 1L;
        ThirdParty thirdParty = createThirdParty(id, "To Activate", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        thirdParty.setIsActive(false);
        
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.of(thirdParty));
        doNothing().when(thirdPartyRepository).update(thirdParty);
        
        thirdPartyService.activateThirdParty(id);
        
        assertTrue(thirdParty.getIsActive());
        verify(thirdPartyRepository).update(thirdParty);
    }

    @Test
    void testGetThirdPartiesCount_ReturnsCount() {
        long count = 5L;
        when(thirdPartyRepository.count()).thenReturn(count);
        
        long result = thirdPartyService.getThirdPartiesCount();
        
        assertEquals(5L, result);
        verify(thirdPartyRepository).count();
    }

    @Test
    void testGetCustomersCount_ReturnsCount() {
        long customerCount = 3L;
        long bothCount = 2L;
        when(thirdPartyRepository.countByType(ThirdParty.ThirdPartyType.CUSTOMER)).thenReturn(customerCount);
        when(thirdPartyRepository.countByType(ThirdParty.ThirdPartyType.BOTH)).thenReturn(bothCount);
        
        long result = thirdPartyService.getCustomersCount();
        
        assertEquals(5L, result);
        verify(thirdPartyRepository).countByType(ThirdParty.ThirdPartyType.CUSTOMER);
        verify(thirdPartyRepository).countByType(ThirdParty.ThirdPartyType.BOTH);
    }

    @Test
    void testGetSuppliersCount_ReturnsCount() {
        long supplierCount = 4L;
        long bothCount = 1L;
        when(thirdPartyRepository.countByType(ThirdParty.ThirdPartyType.SUPPLIER)).thenReturn(supplierCount);
        when(thirdPartyRepository.countByType(ThirdParty.ThirdPartyType.BOTH)).thenReturn(bothCount);
        
        long result = thirdPartyService.getSuppliersCount();
        
        assertEquals(5L, result);
        verify(thirdPartyRepository).countByType(ThirdParty.ThirdPartyType.SUPPLIER);
        verify(thirdPartyRepository).countByType(ThirdParty.ThirdPartyType.BOTH);
    }

    @Test
    void testUpdateBalance_WhenExists_Success() {
        Long id = 1L;
        ThirdParty thirdParty = createThirdParty(id, "Test Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        thirdParty.setCurrentBalance(100.0);
        
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.of(thirdParty));
        doNothing().when(thirdPartyRepository).update(thirdParty);
        
        thirdPartyService.updateBalance(id, 50.0);
        
        assertEquals(150.0, thirdParty.getCurrentBalance());
        verify(thirdPartyRepository).update(thirdParty);
    }

    @Test
    void testUpdateBalance_WithNegativeAmount_Success() {
        Long id = 1L;
        ThirdParty thirdParty = createThirdParty(id, "Test Customer", "12345678901", ThirdParty.ThirdPartyType.CUSTOMER);
        thirdParty.setCurrentBalance(100.0);
        
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.of(thirdParty));
        doNothing().when(thirdPartyRepository).update(thirdParty);
        
        thirdPartyService.updateBalance(id, -30.0);
        
        assertEquals(70.0, thirdParty.getCurrentBalance());
        verify(thirdPartyRepository).update(thirdParty);
    }

    @Test
    void testUpdateBalance_WhenNotExists_ThrowsException() {
        Long id = 999L;
        when(thirdPartyRepository.findById(id)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            thirdPartyService.updateBalance(id, 50.0);
        });
        
        assertTrue(exception.getMessage().contains("ThirdParty not found with ID"));
        verify(thirdPartyRepository, never()).update(any());
    }

    private ThirdParty createThirdParty(Long id, String name, String identification, ThirdParty.ThirdPartyType type) {
        ThirdParty thirdParty = new ThirdParty();
        thirdParty.setId(id);
        thirdParty.setName(name);
        thirdParty.setIdentificationNumber(identification);
        thirdParty.setType(type);
        thirdParty.setEmail("test@example.com");
        thirdParty.setIsActive(true);
        thirdParty.setCurrentBalance(0.0);
        return thirdParty;
    }
}
