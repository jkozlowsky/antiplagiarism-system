package pl.lodz.p.it.ssbd2026.ssbd01.interceptors;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {

    /**
     * Wskazuje wszystkie metody we wszystkich interfejsach rozszerzających Spring Data Repository.
     */
    @Pointcut("execution(public * org.springframework.data.repository.Repository+.*(..))")
    public void allRepositoryMethods() {
    }

    /**
     * Wskazuje metody bezpośrednio oznaczone adnotacją @MethodCallLogged
     * LUB metody znajdujące się w klasach oznaczonych tą adnotacją.
     */
    @Pointcut(
            "execution(@pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged * *(..)) " +
                    "|| within(@pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged *)"
    )
    public void methodCallLoggedAnnotatedMethods() {}

    /**
     * Wskazuje metody bezpośrednio oznaczone adnotacją @TransactionLogged
     * LUB metody znajdujące się w klasach oznaczonych tą adnotacją.
     */
    @Pointcut(
            "execution(@pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged * *(..)) " +
                    "|| within(@pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged *)"
    )
    public void transactionLoggedAnnotatedMethods() {}
}