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

    public Page<AuditRecordDTO> getAuditHistory(int page, int size, String sortBy, boolean sortDesc, String entityFilter) {

        final int DB_FETCH_LIMIT = 1000;

        QueryBuilder jqlQuery = QueryBuilder.anyDomainObject().limit(DB_FETCH_LIMIT);

        if (entityFilter != null && !entityFilter.trim().isEmpty() && !entityFilter.equalsIgnoreCase("ALL")) {
            try {
                Class<?> entityClass = Class.forName("pl.lodz.p.it.ssbd2026.ssbd01.entities." + entityFilter);
                jqlQuery = QueryBuilder.byClass(entityClass).limit(DB_FETCH_LIMIT);
            } catch (ClassNotFoundException e) {
                return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
            }
        }

        Changes changes = javers.findChanges(jqlQuery.build());

        java.util.Map<String, AuditRecordDTO> groupedRecords = new java.util.LinkedHashMap<>();

        for (Change change : changes) {
            CommitMetadata commitMeta = change.getCommitMetadata().orElse(null);
            if (commitMeta == null) continue;

            String commitId = commitMeta.getId().value();

            String rawEntityId = change.getAffectedGlobalId().value();
            String cleanId = rawEntityId.contains("/")
                    ? rawEntityId.substring(rawEntityId.lastIndexOf('/') + 1)
                    : rawEntityId;

            final String entityId = (cleanId.startsWith("\"") && cleanId.endsWith("\""))
                    ? cleanId.substring(1, cleanId.length() - 1)
                    : cleanId;

            String groupKey = commitId + "_" + entityId;

            String fullTypeName = change.getAffectedGlobalId().getTypeName();
            String entityType = fullTypeName.substring(fullTypeName.lastIndexOf('.') + 1);

            AuditRecordDTO dto = groupedRecords.computeIfAbsent(groupKey, k -> {
                AuditRecordDTO newDto = new AuditRecordDTO();
                newDto.setCommitId(commitId);
                newDto.setAuthor(commitMeta.getAuthor());
                newDto.setCommitDate(commitMeta.getCommitDate());
                newDto.setEntityType(entityType);
                newDto.setEntityId(entityId);
                newDto.setChanges(new ArrayList<>());
                newDto.setChangeType("UPDATE");
                return newDto;
            });

            if (change instanceof NewObject) {
                dto.setChangeType("ADD");
            } else if (change instanceof ObjectRemoved) {
                dto.setChangeType("REMOVE");
            } else if (change instanceof ValueChange valChange) {
                String propName = valChange.getPropertyName();
                String oldVal = valChange.getLeft() != null ? valChange.getLeft().toString() : "";
                String newVal = valChange.getRight() != null ? valChange.getRight().toString() : "";

                if ("passwordHash".equals(propName)) {
                    oldVal = "*** UKRYTE ***";
                    newVal = "*** UKRYTE ***";
                }
                dto.getChanges().add(new PropertyChangeDTO(propName, oldVal, newVal));

            } else if (change instanceof ReferenceChange refChange) {
                String propName = refChange.getPropertyName();
                String oldVal = refChange.getLeft() != null ? refChange.getLeft().toString() : "brak";
                String newVal = refChange.getRight() != null ? refChange.getRight().toString() : "brak";

                oldVal = oldVal.contains("/") ? oldVal.substring(oldVal.lastIndexOf('/') + 1) : oldVal;
                newVal = newVal.contains("/") ? newVal.substring(newVal.lastIndexOf('/') + 1) : newVal;

                dto.getChanges().add(new PropertyChangeDTO(propName + " (Relacja)", oldVal, newVal));
            } else if (change instanceof ContainerChange contChange) {
                dto.getChanges().add(new PropertyChangeDTO(contChange.getPropertyName() + " (Kolekcja)", "Zmiana elementÃ³w", ""));
            }
        }


        List<AuditRecordDTO> dtoList = new ArrayList<>(groupedRecords.values());
        dtoList.removeIf(dto -> "UPDATE".equals(dto.getChangeType()) && dto.getChanges().isEmpty());

        Comparator<AuditRecordDTO> comparator;
        if ("entityType".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(AuditRecordDTO::getEntityType);
        } else {
            comparator = Comparator.comparing(AuditRecordDTO::getCommitDate);
        }

        if (sortDesc) {
            comparator = comparator.reversed();
        }
        dtoList.sort(comparator);


        int totalGroupedElements = dtoList.size();
        int start = Math.min(page * size, totalGroupedElements);
        int end = Math.min((page + 1) * size, totalGroupedElements);

        List<AuditRecordDTO> paginatedList = dtoList.subList(start, end);

        return new PageImpl<>(paginatedList, PageRequest.of(page, size), totalGroupedElements);
    }
}