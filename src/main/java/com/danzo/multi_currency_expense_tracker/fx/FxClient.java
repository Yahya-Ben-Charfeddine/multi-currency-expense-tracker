package com.danzo.multi_currency_expense_tracker.fx;

import com.danzo.multi_currency_expense_tracker.common.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

@Service
public class FxClient {
    private final RestClient fxRestClient;

    public FxClient(@Qualifier("fxRestClient") RestClient fxRestClient) {
        this.fxRestClient = fxRestClient;
    }
    public BigDecimal rateToEur(String currency, LocalDate date){
        String curr = currency.toUpperCase(Locale.ROOT);
        if(curr.equals("EUR")){
            return BigDecimal.ONE;
        }
        FxResponse response;
        try {
            response = fxRestClient.get()
                    .uri("/{date}?base={base}&symbols=EUR", date, curr)
                    .retrieve()
                    .body(FxResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new CustomException("Unknown currency: " + curr, HttpStatus.BAD_REQUEST);
        }catch (RestClientResponseException e) {
            throw new CustomException("Exchange rate service returned an error", HttpStatus.BAD_GATEWAY);
        }catch (ResourceAccessException e) {
            throw new CustomException("Exchange rate service is unreachable", HttpStatus.BAD_GATEWAY);
        }
        if (response == null || response.rates() == null) {
            throw new CustomException("Exchange rate service returned no data", HttpStatus.BAD_GATEWAY);
        }
        BigDecimal rate = response.rates().get("EUR");
        if (rate == null) {
            throw new CustomException("Unknown currency: " + curr, HttpStatus.BAD_REQUEST);
        }
        return rate;
    }

}
