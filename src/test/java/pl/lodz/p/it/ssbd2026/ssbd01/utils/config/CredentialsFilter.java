package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;


import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.internal.print.RequestPrinter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.*;

public class CredentialsFilter implements Filter {

    private final Set<String> hideParams = new HashSet<>(Set.of("client_secret", "password"));
    private final String mask = "*****";

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {

        Map<String, String> givenParams = new HashMap<>();

        for (String key : hideParams) {
            String value = requestSpec.getFormParams().get(key);
            if (value != null) {
                givenParams.put(key, value);
                requestSpec.formParam(key, mask);
            }
        }

        RequestPrinter.print(
                requestSpec,
                requestSpec.getMethod(),
                requestSpec.getURI(),
                LogDetail.ALL,
                Collections.emptySet(),
                System.out,
                true
        );

        for (Map.Entry<String, String> entry : givenParams.entrySet()) {
            requestSpec.formParam(entry.getKey(), entry.getValue());
        }

        return ctx.next(requestSpec, responseSpec);
    }
}
