package pl.lodz.p.it.ssbd2026.ssbd01.audit.services;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.commit.CommitId;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.repository.jql.JqlQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.audit.AuditRecordDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private Javers javers;

    @InjectMocks
    private AuditService auditService;

    @Test
    @DisplayName("Pomyślne pobranie pustej historii audytu dla nieistniejącej klasy")
    void shouldReturnEmptyPageForInvalidEntityFilter() {
        Page<AuditRecordDTO> result = auditService.getAuditHistory(0, 20, "commitDate", true, "NonExistentEntity");

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty()),
                () -> verify(javers, never()).findChanges(any())
        );
    }

    @Test
    @DisplayName("Pomyślne pobranie pustej historii audytu, gdy brak zmian w systemie")
    void shouldReturnEmptyPageWhenNoChangesFound() {
        Changes mockChanges = mock(Changes.class);
        when(mockChanges.iterator()).thenReturn(java.util.Collections.emptyIterator());
        when(javers.findChanges(any(JqlQuery.class))).thenReturn(mockChanges);

        Page<AuditRecordDTO> result = auditService.getAuditHistory(0, 20, "commitDate", true, "ALL");

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    @DisplayName("Pomyślne zmaskowanie hasła podczas pobierania historii zmian")
    void shouldObfuscatePasswordHashInAuditHistory() {
        CommitMetadata commitMeta = mock(CommitMetadata.class);
        when(commitMeta.getId()).thenReturn(CommitId.valueOf("1.0"));
        when(commitMeta.getAuthor()).thenReturn("admin");
        when(commitMeta.getCommitDate()).thenReturn(LocalDateTime.now());

        GlobalId globalId = mock(GlobalId.class);
        when(globalId.value()).thenReturn("pl.lodz.p.it.ssbd2026.ssbd01.entities.Account/1");
        when(globalId.getTypeName()).thenReturn("pl.lodz.p.it.ssbd2026.ssbd01.entities.Account");

        ValueChange passwordChange = mock(ValueChange.class);
        when(passwordChange.getCommitMetadata()).thenReturn(Optional.of(commitMeta));
        when(passwordChange.getAffectedGlobalId()).thenReturn(globalId);
        when(passwordChange.getPropertyName()).thenReturn("passwordHash");
        when(passwordChange.getLeft()).thenReturn("old-hash");
        when(passwordChange.getRight()).thenReturn("new-hash");

        Changes mockChanges = mock(Changes.class);
        when(mockChanges.iterator()).thenReturn(List.<Change>of(passwordChange).iterator());

        when(javers.findChanges(any(JqlQuery.class))).thenReturn(mockChanges);

        Page<AuditRecordDTO> result = auditService.getAuditHistory(0, 20, "commitDate", true, "ALL");

        assertAll(
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals(1, result.getContent().getFirst().getChanges().size()),
                () -> assertEquals("*** UKRYTE ***", result.getContent().getFirst().getChanges().getFirst().getOldValue()),
                () -> assertEquals("*** UKRYTE ***", result.getContent().getFirst().getChanges().getFirst().getNewValue())
        );
    }
}