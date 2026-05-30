package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherSearchDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.TeacherRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@MethodCallLogged
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

        return teacherRepository.findByAccountLoginContainingIgnoreCaseOrAccountEmailContainingIgnoreCase(query, query)
                .stream()
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .map(t -> new TeacherSearchDTO(
                        t.getAccount().getLogin(),
                        t.getAccount().getName(),
                        t.getAccount().getSurname(),
                        t.getAccount().getEmail()
                ))
                .toList();
    }
}