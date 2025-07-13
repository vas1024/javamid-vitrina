package javamid.vitrina.app.restclient.api;

import javamid.vitrina.app.restclient.ApiClient;

import javamid.vitrina.app.restclient.model.PaymentRequest;
import javamid.vitrina.app.restclient.model.PaymentResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-07-12T16:59:33.605817900+03:00[Europe/Moscow]", comments = "Generator version: 7.12.0")
public class PaymentApi {
    private ApiClient apiClient;

    public PaymentApi() {
        this(new ApiClient());
    }

    @Autowired
    public PaymentApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Создать платеж
     * 
     * <p><b>200</b> - Платеж создан
     * <p><b>400</b> - Неверные данные
     * @param paymentRequest The paymentRequest parameter
     * @return PaymentResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec paymentPostRequestCreation(PaymentRequest paymentRequest) throws WebClientResponseException {
        Object postBody = paymentRequest;
        // verify the required parameter 'paymentRequest' is set
        if (paymentRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'paymentRequest' when calling paymentPost", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<PaymentResponse> localVarReturnType = new ParameterizedTypeReference<PaymentResponse>() {};
        return apiClient.invokeAPI("/payment", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Создать платеж
     * 
     * <p><b>200</b> - Платеж создан
     * <p><b>400</b> - Неверные данные
     * @param paymentRequest The paymentRequest parameter
     * @return PaymentResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PaymentResponse> paymentPost(PaymentRequest paymentRequest) throws WebClientResponseException {
        ParameterizedTypeReference<PaymentResponse> localVarReturnType = new ParameterizedTypeReference<PaymentResponse>() {};
        return paymentPostRequestCreation(paymentRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Создать платеж
     * 
     * <p><b>200</b> - Платеж создан
     * <p><b>400</b> - Неверные данные
     * @param paymentRequest The paymentRequest parameter
     * @return ResponseEntity&lt;PaymentResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PaymentResponse>> paymentPostWithHttpInfo(PaymentRequest paymentRequest) throws WebClientResponseException {
        ParameterizedTypeReference<PaymentResponse> localVarReturnType = new ParameterizedTypeReference<PaymentResponse>() {};
        return paymentPostRequestCreation(paymentRequest).toEntity(localVarReturnType);
    }

    /**
     * Создать платеж
     * 
     * <p><b>200</b> - Платеж создан
     * <p><b>400</b> - Неверные данные
     * @param paymentRequest The paymentRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec paymentPostWithResponseSpec(PaymentRequest paymentRequest) throws WebClientResponseException {
        return paymentPostRequestCreation(paymentRequest);
    }
}
