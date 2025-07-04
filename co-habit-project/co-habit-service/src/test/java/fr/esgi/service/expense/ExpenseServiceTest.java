package fr.esgi.service.expense;

import fr.esgi.domain.dto.expense.ExpenseReqDto;
import fr.esgi.domain.dto.expense.ExpenseResDto;
import fr.esgi.domain.dto.expense.PaymentValidationReqDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.persistence.entity.expense.Expense;
import fr.esgi.persistence.entity.expense.ExpenseParticipant;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.expense.ExpenseParticipantRepository;
import fr.esgi.persistence.repository.expense.ExpenseRepository;
import fr.esgi.persistence.repository.space.ColocationRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractTest;
import fr.esgi.service.expense.mapper.ExpenseMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(ExpenseServiceTest.TestConfig.class)
@TestPropertySource(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
@EnableJpaRepositories(basePackages = "fr.esgi.persistence.repository")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExpenseServiceTest extends AbstractTest {

    @TestConfiguration
    @EnableAutoConfiguration(
            exclude = {
                    ServletWebServerFactoryAutoConfiguration.class,
                    ReactiveWebServerFactoryAutoConfiguration.class
            }
    )
    static class TestConfig {
        @Bean
        public ExpenseService expenseService(
                ExpenseRepository expenseRepository,
                ExpenseParticipantRepository expenseParticipantRepository,
                ColocationRepository colocationRepository,
                UserRepository userRepository) {
            return new ExpenseService(
                    expenseRepository,
                    expenseParticipantRepository,
                    colocationRepository,
                    userRepository,
                    Mappers.getMapper(ExpenseMapper.class)
            );
        }
    }

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseParticipantRepository expenseParticipantRepository;

    @Autowired
    private ColocationRepository colocationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseService expenseService;

    private User payerUser;
    private User participantUser;
    private User otherUser;
    private Colocation colocation;

    @BeforeEach
    public void initData() {
        // Initialize security context with test user
        initSecurityContextPlaceHolder();
        
        // Setup payer user
        payerUser = new User();
        payerUser.setEmail("payer@example.com");
        payerUser.setFirstName("John");
        payerUser.setLastName("Payer");
        payerUser.setKeyCloakSub(TEST_USER_ID);
        payerUser.setBirthDate(LocalDate.of(1990, 1, 1));
        payerUser = userRepository.save(payerUser);

        // Setup participant user
        participantUser = new User();
        participantUser.setEmail("participant@example.com");
        participantUser.setFirstName("Jane");
        participantUser.setLastName("Participant");
        participantUser.setKeyCloakSub("participant-sub");
        participantUser.setBirthDate(LocalDate.of(1992, 1, 1));
        participantUser = userRepository.save(participantUser);

        // Setup other user
        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setFirstName("Bob");
        otherUser.setLastName("Other");
        otherUser.setKeyCloakSub("other-sub");
        otherUser.setBirthDate(LocalDate.of(1988, 1, 1));
        otherUser = userRepository.save(otherUser);

        // Setup colocation
        colocation = new Colocation();
        colocation.setName("Test Colocation");
        colocation.setAddress("123 Test Street");
        colocation.setCity("Test City");
        colocation.setPostalCode("12345");
        colocation.setManager(payerUser);
        colocation.setInvitationCode("TEST123");
        colocation.setRoommates(new HashSet<>());
        colocation.addRoommate(payerUser);
        colocation.addRoommate(participantUser);
        colocation = colocationRepository.save(colocation);
    }

    @AfterEach
    public void cleanUp() {
        // Clean up security context
        cleanupSecurityContext();
        
        // Clean up database
        expenseParticipantRepository.deleteAll();
        expenseRepository.deleteAll();
        colocationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testCreateExpense_Success() throws TechnicalException {
        // Given
        ExpenseReqDto dto = new ExpenseReqDto();
        dto.setTitle("Test Expense");
        dto.setDescription("Test Description");
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setSpaceId(colocation.getId());
        dto.setParticipantIds(Set.of(payerUser.getId(), participantUser.getId()));

        // When
        ExpenseResDto result = expenseService.createExpense(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Expense");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(result.getSpaceId()).isEqualTo(colocation.getId());
        assertThat(result.getPayer().getEmail()).isEqualTo(payerUser.getEmail());
        assertThat(result.getParticipants()).hasSize(2);
        assertThat(result.isSettled()).isFalse();
    }

    @Test
    public void testCreateExpense_UserNotFound() {
        // Given
        ExpenseReqDto dto = new ExpenseReqDto();
        dto.setTitle("Test Expense");
        dto.setDescription("Test Description");
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setSpaceId(colocation.getId());

        // Clean up security context and set an unknown user
        cleanupSecurityContext();
        initSecurityContextPlaceHolderWithSub("unknown-user-sub");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            expenseService.createExpense(dto);
        });
        assertThat(exception.getCode()).isEqualTo(404);
        assertThat(exception.getMessage()).contains("Utilisateur non trouvé");
        
        // Reset to original test user
        cleanupSecurityContext();
        initSecurityContextPlaceHolder();
    }

    @Test
    public void testCreateExpense_ColocationNotFound() {
        // Given
        ExpenseReqDto dto = new ExpenseReqDto();
        dto.setTitle("Test Expense");
        dto.setDescription("Test Description");
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setSpaceId(999L);

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            expenseService.createExpense(dto);
        });
        assertThat(exception.getCode()).isEqualTo(404);
        assertThat(exception.getMessage()).contains("Colocation non trouvée");
    }

    @Test
    public void testCreateExpense_AccessDenied_NotMember() {
        // Given
        ExpenseReqDto dto = new ExpenseReqDto();
        dto.setTitle("Test Expense");
        dto.setDescription("Test Description");
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setSpaceId(colocation.getId());

        // Create a colocation where the user is not a member
        Colocation otherColocation = new Colocation();
        otherColocation.setName("Other Colocation");
        otherColocation.setAddress("456 Other Street");
        otherColocation.setCity("Other City");
        otherColocation.setPostalCode("67890");
        otherColocation.setManager(otherUser);
        otherColocation.setInvitationCode("OTHER123");
        otherColocation.setRoommates(new HashSet<>());
        otherColocation.addRoommate(otherUser);
        otherColocation = colocationRepository.save(otherColocation);

        dto.setSpaceId(otherColocation.getId());
        
        // Make sure the security context has the main test user who is not a member of otherColocation
        cleanupSecurityContext();
        initSecurityContextPlaceHolder(); // TEST_USER_ID is not a member of otherColocation

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            expenseService.createExpense(dto);
        });
        assertThat(exception.getCode()).isEqualTo(403);
        assertThat(exception.getMessage()).contains("Accès refusé");
    }

    @Test
    public void testGetExpensesBySpace_Success() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expenseRepository.save(expense);

        // When
        List<ExpenseResDto> result = expenseService.getExpensesBySpace(colocation.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Expense");
    }

    @Test
    public void testGetExpenseById_Success() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expense = expenseRepository.save(expense);

        // When
        ExpenseResDto result = expenseService.getExpenseById(expense.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Expense");
        assertThat(result.getId()).isEqualTo(expense.getId());
    }

    @Test
    public void testGetExpenseById_NotFound() {
        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            expenseService.getExpenseById(999L);
        });
        assertThat(exception.getCode()).isEqualTo(404);
        assertThat(exception.getMessage()).contains("Dépense non trouvée");
    }

    @Test
    public void testValidatePayment_Success() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expense = expenseRepository.save(expense);

        PaymentValidationReqDto dto = new PaymentValidationReqDto();
        dto.setPaymentMethod("Virement bancaire");

        // When
        ExpenseResDto result = expenseService.validatePayment(expense.getId(), dto);

        // Then
        assertThat(result).isNotNull();
        Optional<ExpenseParticipant> participant = expenseParticipantRepository.findByExpenseAndUser(expense, payerUser);
        assertThat(participant).isPresent();
        assertThat(participant.get().isValidated()).isTrue();
        assertThat(participant.get().getPaymentMethod()).isEqualTo("Virement bancaire");
    }

    @Test
    public void testValidatePayment_ParticipantNotFound() {
        // Given
        Expense expense = createTestExpense();
        expense.setPayer(participantUser); // Different payer
        expense = expenseRepository.save(expense);

        // Clean up and setup a different user context that is not a participant
        cleanupSecurityContext();
        initSecurityContextPlaceHolderWithSub(otherUser.getKeyCloakSub());

        PaymentValidationReqDto dto = new PaymentValidationReqDto();
        dto.setPaymentMethod("Virement bancaire");

        // When & Then
        final Expense finalExpense = expense;
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            expenseService.validatePayment(finalExpense.getId(), dto);
        });
        assertThat(exception.getCode()).isEqualTo(404);
        assertThat(exception.getMessage()).contains("Vous n'êtes pas participant à cette dépense");
        
        // Reset to original test user
        cleanupSecurityContext();
        initSecurityContextPlaceHolder();
    }

    @Test
    public void testConfirmPayment_Success() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expense = expenseRepository.save(expense);

        // When
        ExpenseResDto result = expenseService.confirmPayment(expense.getId(), payerUser.getId());

        // Then
        assertThat(result).isNotNull();
        Optional<ExpenseParticipant> participant = expenseParticipantRepository.findByExpenseAndUser(expense, payerUser);
        assertThat(participant).isPresent();
        assertThat(participant.get().isConfirmedByCreator()).isTrue();
    }

    @Test
    public void testConfirmPayment_NotCreator() {
        // Given
        Expense expense = createTestExpense();
        expense.setPayer(participantUser); // Different payer
        expense = expenseRepository.save(expense);
        
        // The current user (TEST_USER_ID/payerUser) is trying to confirm payment
        // but the expense payer is participantUser, so it should fail
        // Keep the original security context (TEST_USER_ID)

        // When & Then
        final Expense finalExpense = expense;
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            expenseService.confirmPayment(finalExpense.getId(), payerUser.getId());
        });
        assertThat(exception.getCode()).isEqualTo(403);
        assertThat(exception.getMessage()).contains("Seul le créateur de la dépense peut confirmer les paiements");
    }

    @Test
    public void testConfirmAllPayments_Success() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expense = expenseRepository.save(expense);

        // When
        ExpenseResDto result = expenseService.confirmAllPayments(expense.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSettled()).isTrue();

        List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpense(expense);
        assertThat(participants).allMatch(ExpenseParticipant::isConfirmedByCreator);
    }

    @Test
    public void testDeleteExpense_Success() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expense = expenseRepository.save(expense);
        Long expenseId = expense.getId();

        // When
        expenseService.deleteExpense(expenseId);

        // Then
        Optional<Expense> deletedExpense = expenseRepository.findById(expenseId);
        assertThat(deletedExpense).isEmpty();
    }

    @Test
    public void testDeleteExpense_NotCreator() {
        // Given
        Expense expense = createTestExpense();
        expense.setPayer(participantUser); // Different payer
        expense = expenseRepository.save(expense);
        
        // The current user (TEST_USER_ID/payerUser) is trying to delete
        // but the expense payer is participantUser, so it should fail
        // Keep the original security context (TEST_USER_ID)

        // When & Then
        final Expense finalExpense = expense;
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            expenseService.deleteExpense(finalExpense.getId());
        });
        assertThat(exception.getCode()).isEqualTo(403);
        assertThat(exception.getMessage()).contains("Seul le créateur de la dépense peut la supprimer");
    }

    @Test
    public void testDeleteExpense_AlreadySettled() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expense.confirmAllPayments(); // Make it settled
        expense = expenseRepository.save(expense);
        
        // Reset security context to main user (creator of expense)
        cleanupSecurityContext();
        initSecurityContextPlaceHolder();

        // When & Then
        final Expense finalExpense = expense;
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            expenseService.deleteExpense(finalExpense.getId());
        });
        assertThat(exception.getCode()).isEqualTo(400);
        assertThat(exception.getMessage()).contains("Impossible de supprimer une dépense déjà réglée");
    }

    @Test
    public void testGetUserExpenses_Success() throws TechnicalException {
        // Given
        Expense expense1 = createTestExpense();
        expense1.setTitle("Expense 1");
        expenseRepository.save(expense1);

        Expense expense2 = createTestExpense();
        expense2.setTitle("Expense 2");
        expense2.setPayer(participantUser);
        expenseRepository.save(expense2);

        // When
        List<ExpenseResDto> result = expenseService.getUserExpenses();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(ExpenseResDto::getTitle))
                .containsExactlyInAnyOrder("Expense 1", "Expense 2");
    }

    @Test
    public void testGetPendingPayments_Success() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expense = expenseRepository.save(expense);

        // When
        List<ExpenseResDto> result = expenseService.getPendingPayments();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Expense");
    }

    @Test
    public void testGetPendingConfirmations_Success() throws TechnicalException {
        // Given
        Expense expense = createTestExpense();
        expense = expenseRepository.save(expense);

        // Validate payment by participant
        ExpenseParticipant participant = expenseParticipantRepository.findByExpenseAndUser(expense, payerUser)
                .orElseThrow();
        participant.validate("Virement bancaire");
        expenseParticipantRepository.save(participant);

        // When
        List<ExpenseResDto> result = expenseService.getPendingConfirmations();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Expense");
    }

    private Expense createTestExpense() {
        Expense expense = new Expense();
        expense.setTitle("Test Expense");
        expense.setDescription("Test Description");
        expense.setAmount(BigDecimal.valueOf(100.00));
        expense.setPayer(payerUser);
        expense.setSpace(colocation);

        Set<User> participants = Set.of(payerUser, participantUser);
        expense.distributeEvenly(participants);

        return expense;
    }
}
