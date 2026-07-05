package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RuleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.ReportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetRulePresetsServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private RuleMapper ruleMapper;

    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private RuleService ruleService;

    @Test
    @DisplayName("Powinno pomyślnie pobrać listę szablonów")
    public void shouldGetRulePresetsTemplatesSuccessfully() {
        ReportLevelDict raportLevel = new ReportLevelDict();
        raportLevel.setName("FULL_VIEW");

        RulePreset template = new RulePreset();
        ReflectionTestUtils.setField(template, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(template, "version", 1);
        template.setIsPreset(true);
        template.setRaportLevel(raportLevel);
        template.setStudentTicketCount(10);

        RulePresetDTO dto = new RulePresetDTO(template.getId(), "FULL_VIEW", 10, 1, true,  "hash123");

        when(ruleRepository.findAllByIsPreset(true)).thenReturn(List.of(template));
        when(ruleMapper.toRuleDTO(template)).thenReturn(dto);

        when(versionHashService.hashObjectVersion(template.getId(), template.getVersion())).thenReturn("hash123");

        List<RulePresetDTO> result = ruleService.getRulePresetsTemplates();

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("FULL_VIEW", result.get(0).raportLevelName()),
                () -> assertEquals(10, result.get(0).studentTicketCount()),
                () -> assertEquals("hash123", result.get(0).versionHash())
        );
    }
}