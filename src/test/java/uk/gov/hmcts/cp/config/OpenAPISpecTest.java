package uk.gov.hmcts.cp.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.api.CasesApi;
import uk.gov.hmcts.cp.openapi.model.ErrorResponse;
import uk.gov.hmcts.cp.openapi.model.Response;
import uk.gov.hmcts.cp.openapi.model.Result;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OpenAPISpecTest {
    @Test
    void generated_error_response_should_have_expected_fields() {
        assertThat(ErrorResponse.class).hasDeclaredFields("error", "message", "details", "traceId");
    }

    @Test
    void generated_response_should_have_expected_fields() {
        assertThat(Response.class).hasDeclaredFields("results");
    }

    @Test
    void generated_result_should_have_expected_fields() {
        assertThat(Result.class).hasDeclaredFields("resultText");
    }

    @Test
    void generated_api_should_have_expected_methods() {
        assertThat(CasesApi.class).hasDeclaredMethods("getResults");
    }
}