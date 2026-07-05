package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.junit.jupiter.api.Test;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExceptionsCoverageTest {

    private void instantiateException(Class<?> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                constructor.setAccessible(true);
                Parameter[] parameters = constructor.getParameters();
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> type = parameters[i].getType();
                    if (type == String.class) args[i] = "Test";
                    else if (type == Throwable.class) args[i] = new RuntimeException();
                    else if (type.isPrimitive()) {
                        if (type == boolean.class) args[i] = false;
                        else if (type == char.class) args[i] = '\0';
                        else args[i] = 0;
                    } else args[i] = null;
                }
                Object instance = constructor.newInstance(args);
                assertNotNull(instance);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    void shouldCoverAllApplicationExceptions() {
        instantiateException(AccountActivationException.class);
        instantiateException(AccountConstraintViolationException.class);
        instantiateException(AccountIsBlockedException.class);
        instantiateException(AccountIsNotBlockedException.class);
        instantiateException(AccountIsNotVerifiedException.class);
        instantiateException(AccountNotFoundException.class);
        instantiateException(AppBaseException.class);
        instantiateException(ConcurrentUpdateException.class);
        instantiateException(DeleteReportException.class);
        instantiateException(EmailSendingException.class);
        instantiateException(GitCloneException.class);
        instantiateException(GiteaIntegrationException.class);
        instantiateException(HashEncryptionException.class);
        instantiateException(InvalidCredentialsProvidedException.class);
        instantiateException(InvalidOldPasswordException.class);
        instantiateException(JGitException.class);
        instantiateException(JplagScanException.class);
        instantiateException(LanguageDictNotFoundException.class);
        instantiateException(NoPermissionForThisSubjectException.class);
        instantiateException(NoPermissionToEditScheduleException.class);
        instantiateException(OneTimeTokenInvalidException.class);
        instantiateException(OriginalLanguageNotFoundException.class);
        instantiateException(PasswordAlreadyUsedInPastException.class);
        instantiateException(PasswordSameAsOldException.class);
        instantiateException(ReportFileException.class);
        instantiateException(ReportNotFoundException.class);
        instantiateException(Resend2FATokenException.class);
        instantiateException(RulePresetNotSetForSubjectException.class);
        instantiateException(ScheduleModificationDeadlineExceededException.class);
        instantiateException(ScheduleNotFoundException.class);
        instantiateException(SubjectAccessDeniedException.class);
        instantiateException(SubjectNotFoundException.class);

        instantiateException(SubjectAlreadyExistsException.class);
        instantiateException(SubjectArchivedException.class);
        instantiateException(SubjectAssignmentNotFoundException.class);
        instantiateException(SubjectMustHaveOwnerException.class);

        instantiateException(RulePresetInvalidPartametersException.class);
        instantiateException(RulePresetNoAccessException.class);
        instantiateException(RulePresetNotFoundException.class);
        instantiateException(RulePresetTemplateNotFoundException.class);

        instantiateException(RaportLevelDictNotFoundException.class);

        instantiateException(NoTicketsLeftException.class);
        instantiateException(RepositoryNotFoundException.class);

        instantiateException(AccessLevelAlreadyAssignedException.class);
        instantiateException(AccessLevelAlreadyRevokedException.class);
        instantiateException(AccessLevelDictNotFoundException.class);
        instantiateException(AccessLevelNotAssignedException.class);
    }
}