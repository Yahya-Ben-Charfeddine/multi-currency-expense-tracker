package com.danzo.multi_currency_expense_tracker.summary;

import com.danzo.multi_currency_expense_tracker.auth.UserRepo;
import com.danzo.multi_currency_expense_tracker.auth.Users;
import com.danzo.multi_currency_expense_tracker.common.CustomException;
import com.danzo.multi_currency_expense_tracker.expense.Category;
import com.danzo.multi_currency_expense_tracker.expense.ExpenseRepo;
import com.danzo.multi_currency_expense_tracker.expense.ExpenseResponse;
import com.danzo.multi_currency_expense_tracker.expense.ExpenseService;
import com.danzo.multi_currency_expense_tracker.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {
    private static final Logger log = LoggerFactory.getLogger(SummaryService.class);
    private final ExpenseService expenseService;
    private static final String SYSTEM_PROMPT = """
            You are a personal finance assistant. You receive a summary of one user's
            spending for one month, already calculated. Do not recalculate the numbers.

            Respond ONLY with a JSON object. No markdown, no code fences, no text before
            or after it. Use exactly these keys:
            - "headline": one short sentence summarizing the month (max 15 words)
            - "unusualSpending": an array of short strings naming notable or unusually
              high spending; use an empty array if nothing stands out
            - "savingTip": one concrete, practical saving tip based on the numbers
            """;
    private final LlmClient llmClient;
    private final JsonMapper jsonMapper;


    public SummaryService(UserRepo userRepo, ExpenseRepo expenseRepo, ExpenseService expenseService, LlmClient llmClient, JsonMapper jsonMapper) {
        this.expenseService = expenseService;
        this.llmClient = llmClient;
        this.jsonMapper = jsonMapper;
    }

    public MonthlyStats computeStats(String email, YearMonth month) {
        List<ExpenseResponse> expenses = expenseService.list(email, month);

        long total = expenses.stream()
                .mapToLong(ExpenseResponse::amountEurCents)
                .sum();

        Map<Category, Long> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseResponse::category,
                        () -> new EnumMap<>(Category.class),
                        Collectors.summingLong(ExpenseResponse::amountEurCents)
                ));

        ExpenseResponse biggest = expenses.stream()
                .max(Comparator.comparingLong(ExpenseResponse::amountEurCents))
                .orElse(null);

        return new MonthlyStats(total, byCategory, expenses.size(), biggest);
    }

    public SummaryResponse summarize(String email, YearMonth month) {
        MonthlyStats stats = computeStats(email, month);
        AiInsight ai = stats.count() == 0 ? null : tryAiInsight(month, stats);
        return new SummaryResponse(month, stats.totalEurCents(), stats.byCategory(), ai);
    }
    private AiInsight tryAiInsight(YearMonth month, MonthlyStats stats) {
        try {
            String answer = llmClient.complete(SYSTEM_PROMPT, buildUserPrompt(month, stats));
            AiInsight insight = jsonMapper.readValue(extractJson(answer), AiInsight.class);
            if (insight.headline() == null || insight.savingTip() == null) {
                throw new IllegalStateException("AI answer is missing required fields");
            }
            return insight;
        } catch (Exception e) {
            log.warn("AI summary failed for {}: {}", month, e.getMessage());
            return null;
        }
    }
    private String buildUserPrompt(YearMonth month, MonthlyStats stats) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Month: ").append(month).append('\n');
        prompt.append("Total spent: ").append(euros(stats.totalEurCents()))
                .append(" EUR across ").append(stats.count()).append(" expenses\n");
        prompt.append("Spending by category:\n");
        stats.byCategory().forEach((category, cents) ->
                prompt.append("- ").append(category).append(": ").append(euros(cents)).append(" EUR\n"));
        if (stats.biggest() != null) {
            prompt.append("Biggest single expense: ").append(euros(stats.biggest().amountEurCents()))
                    .append(" EUR in category ").append(stats.biggest().category()).append('\n');
        }
        return prompt.toString();
    }
    private static BigDecimal euros(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }
    private static String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start == -1 || end <= start) {
            throw new IllegalStateException("No JSON object found in AI answer");
        }
        return text.substring(start, end + 1);
    }
}
