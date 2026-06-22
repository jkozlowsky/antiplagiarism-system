package pl.lodz.p.it.ssbd2026.ssbd01.audit.services;

import lombok.RequiredArgsConstructor;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ContainerChange;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.audit.AuditRecordDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.audit.PropertyChangeDTO;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final Javers javers;
    private static final int DB_FETCH_LIMIT = 100;
    private static final String ENTITY_PACKAGE = "pl.lodz.p.it.ssbd2026.ssbd01.entities.";
    private static final String HIDDEN_PASSWORD_TEXT = "*** UKRYTE ***";

    public Page<AuditRecordDTO> getAuditHistory(int page, int size, String sortBy, boolean sortDesc, String entityFilter) {
        QueryBuilder queryBuilder = buildJaversQuery(entityFilter);
        if (queryBuilder == null) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
        }

        Changes changes = javers.findChanges(queryBuilder.build());
        List<AuditRecordDTO> auditRecords = processAndGroupChanges(changes);

        sortRecords(auditRecords, sortBy, sortDesc);

        return paginateRecords(auditRecords, page, size);
    }

    private QueryBuilder buildJaversQuery(String entityFilter) {
        if (entityFilter == null || entityFilter.trim().isEmpty() || "ALL".equalsIgnoreCase(entityFilter)) {
            return QueryBuilder.anyDomainObject().limit(DB_FETCH_LIMIT);
        }

        try {
            Class<?> entityClass = Class.forName(ENTITY_PACKAGE + entityFilter);
            return QueryBuilder.byClass(entityClass).limit(DB_FETCH_LIMIT);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private List<AuditRecordDTO> processAndGroupChanges(Changes changes) {
        Map<String, AuditRecordDTO> groupedRecords = new LinkedHashMap<>();

        for (Change change : changes) {
            change.getCommitMetadata().ifPresent(commitMeta -> {
                String commitId = commitMeta.getId().value();
                String entityId = extractCleanEntityId(change.getAffectedGlobalId().value());
                String entityType = extractEntityType(change.getAffectedGlobalId().getTypeName());
                String groupKey = commitId + "_" + entityId;

                AuditRecordDTO dto = groupedRecords.computeIfAbsent(groupKey, k ->
                        createBaseAuditRecord(commitMeta, entityId, entityType));

                applyChangeDetails(dto, change);
            });
        }

        List<AuditRecordDTO> dtoList = new ArrayList<>(groupedRecords.values());
        dtoList.removeIf(dto -> "UPDATE".equals(dto.getChangeType()) && dto.getChanges().isEmpty());
        return dtoList;
    }

    private AuditRecordDTO createBaseAuditRecord(CommitMetadata commitMeta, String entityId, String entityType) {
        AuditRecordDTO dto = new AuditRecordDTO();
        dto.setCommitId(commitMeta.getId().value());
        dto.setAuthor(commitMeta.getAuthor());
        dto.setCommitDate(commitMeta.getCommitDate());
        dto.setEntityType(entityType);
        dto.setEntityId(entityId);
        dto.setChanges(new ArrayList<>());
        dto.setChangeType("UPDATE");
        return dto;
    }


    private void applyChangeDetails(AuditRecordDTO dto, Change change) {
        if (change instanceof NewObject) {
            dto.setChangeType("ADD");
        } else if (change instanceof ObjectRemoved) {
            dto.setChangeType("REMOVE");
        } else if (change instanceof ValueChange valChange) {
            handleValueChange(dto, valChange);
        } else if (change instanceof ReferenceChange refChange) {
            handleReferenceChange(dto, refChange);
        } else if (change instanceof ContainerChange contChange) {
            dto.getChanges().add(new PropertyChangeDTO(contChange.getPropertyName() + " (Kolekcja)", "Zmiana elementów", ""));
        }
    }

    private void handleValueChange(AuditRecordDTO dto, ValueChange change) {
        String propName = change.getPropertyName();
        String oldVal = change.getLeft() != null ? change.getLeft().toString() : "";
        String newVal = change.getRight() != null ? change.getRight().toString() : "";

        if ("passwordHash".equals(propName)) {
            oldVal = HIDDEN_PASSWORD_TEXT;
            newVal = HIDDEN_PASSWORD_TEXT;
        }

        dto.getChanges().add(new PropertyChangeDTO(propName, oldVal, newVal));
    }

    private void handleReferenceChange(AuditRecordDTO dto, ReferenceChange change) {
        String propName = change.getPropertyName() + " (Relacja)";
        String oldVal = change.getLeft() != null ? extractPathSuffix(change.getLeft().toString()) : "brak";
        String newVal = change.getRight() != null ? extractPathSuffix(change.getRight().toString()) : "brak";

        dto.getChanges().add(new PropertyChangeDTO(propName, oldVal, newVal));
    }

    private String extractCleanEntityId(String rawEntityId) {
        String cleanId = extractPathSuffix(rawEntityId);
        if (cleanId.startsWith("\"") && cleanId.endsWith("\"")) {
            return cleanId.substring(1, cleanId.length() - 1);
        }
        return cleanId;
    }

    private String extractEntityType(String fullTypeName) {
        return fullTypeName.substring(fullTypeName.lastIndexOf('.') + 1);
    }

    private String extractPathSuffix(String path) {
        return path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
    }

    private void sortRecords(List<AuditRecordDTO> records, String sortBy, boolean sortDesc) {
        Comparator<AuditRecordDTO> comparator = "entityType".equalsIgnoreCase(sortBy)
                ? Comparator.comparing(AuditRecordDTO::getEntityType)
                : Comparator.comparing(AuditRecordDTO::getCommitDate);

        if (sortDesc) {
            comparator = comparator.reversed();
        }

        records.sort(comparator);
    }

    private Page<AuditRecordDTO> paginateRecords(List<AuditRecordDTO> records, int page, int size) {
        int total = records.size();
        int start = Math.min(page * size, total);
        int end = Math.min((page + 1) * size, total);

        List<AuditRecordDTO> paginatedList = records.subList(start, end);
        return new PageImpl<>(paginatedList, PageRequest.of(page, size), total);
    }
}