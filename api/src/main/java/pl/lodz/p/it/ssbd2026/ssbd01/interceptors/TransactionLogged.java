package pl.lodz.p.it.ssbd2026.ssbd01.interceptors;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TransactionLogged {

}