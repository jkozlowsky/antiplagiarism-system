package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;

import static org.junit.jupiter.api.Assertions.*;

public class AccessLevelModelTest {

    @Test
    @DisplayName("Poprawne przypisanie konta i słownika dla nowej roli Teacher z domyślną flagą isActive na true")
    void shouldCorrectlyAssignAccountAndDictToTeacher() {
        Account account = new Account();
        account.setLogin("modelTest");

        AccessLevelDict dict = new AccessLevelDict();
        dict.setAccessLevelName(AccountConsts.ROLE_TEACHER);

        Teacher teacher = new Teacher();
        teacher.setAccount(account);
        teacher.setAccessLevelDict(dict);

        assertAll(
                () -> assertEquals("modelTest", teacher.getAccount().getLogin()),
                () -> assertEquals(AccountConsts.ROLE_TEACHER, teacher.getAccessLevelDict().getAccessLevelName()),
                () -> assertTrue(teacher.getActive())
        );
    }

    @Test
    @DisplayName("Weryfikacja wartości wersji nowej roli jako null")
    void shouldInitializeVersionWithNull() {
        Admin admin = new Admin();
        assertNull(admin.getVersion());
    }

    @Test
    @DisplayName("Weryfikacja, czy instancje różnych ról dostępu są odrębnymi klasami")
    void instancesOfDifferentAccessLevelsShouldNotBeEqual() {
        Teacher teacher = new Teacher();
        Student student = new Student();
        Admin admin = new Admin();

        assertAll(
                () -> assertNotEquals(teacher.getClass(), student.getClass()),
                () -> assertNotEquals(student.getClass(), admin.getClass())
        );
    }
}