package javamid.vitrina.app.restclient.api;

import javamid.vitrina.app.restclient.ApiClient;

import javamid.vitrina.app.restclient.model.Balance;

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
public class BalanceApi {
    private ApiClient apiClient;

    public BalanceApi() {
        this(new ApiClient());
    }

    @Autowired
    public BalanceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Получить баланс пользователя
     * 
     * <p><b>200</b> - Успешный запрос
     * @param userId The userId parameter
     * @return Balance
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec paymentBalanceUserIdGetRequestCreation(String userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling paymentBalanceUserIdGet", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Balance> localVarReturnType = new ParameterizedTypeReference<Balance>() {};
        return apiClient.invokeAPI("/payment/balance/{userId}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Получить баланс пользователя
     * 
     * <p><b>200</b> - Успешный запрос
     * @param userId The userId parameter
     * @return Balance
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Balance> paymentBalanceUserIdGet(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<Balance> localVarReturnType = new ParameterizedTypeReference<Balance>() {};
        return paymentBalanceUserIdGetRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Получить баланс пользователя
     * 
     * <p><b>200</b> - Успешный запрос
     * @param userId The userId parameter
     * @return ResponseEntity&lt;Balance&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Balance>> paymentBalanceUserIdGetWithHttpInfo(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<Balance> localVarReturnType = new ParameterizedTypeReference<Balance>() {};
        return paymentBalanceUserIdGetRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Получить баланс пользователя
     * 
     * <p><b>200</b> - Успешный запрос
     * @param userId The userId parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec paymentBalanceUserIdGetWithResponseSpec(String userId) throws WebClientResponseException {
        return paymentBalanceUserIdGetRequestCreation(userId);
    }
}
