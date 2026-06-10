package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherSearchDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.TeacherRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
public class TeacherService {

    private final TeacherRepository teacherRepository;

    @MethodCallLogged
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public List<TeacherSearchDTO> searchTeachers(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }

        return teacherRepository.findProjectedActiveTeachersByQuery(query)
                .stream()
                .map(t -> new TeacherSearchDTO(
                        t.login(),
                        t.name(),
                        t.surname(),
                        t.email()
                ))
                .toList();
    }
}