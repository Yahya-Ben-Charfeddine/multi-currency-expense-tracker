# Multi-Currency Expense Tracker

When you travel or shop online, your spending ends up scattered across currencies: lunch in dollars, a train ticket in pounds, groceries in euros. Adding it all up at the end of the month is annoying.

This API solves that. You log each expense in whatever currency you paid in, and it gets converted to euros using the real exchange rate from the day you spent it. At the end of the month, you can ask for a summary: totals per category, plus a short comment from an AI about how the month went.

I built it as a backend project to get hands-on with Spring Security, external APIs, money handling and LLM integration in one realistic app.

## What it's built with

- **Java 21** and **Spring Boot 4**
- **Spring Security** with JWT login (jjwt)
- **PostgreSQL** with Spring Data JPA
- **[Frankfurter](https://frankfurter.dev)** for historical exchange rates, based on European Central Bank data
- **[Ollama](https://ollama.com)** running `llama3.2` locally for the AI summary. It talks to Ollama through the OpenAI-compatible API, so switching to another provider is just a config change.

## The API

| Method | Path | What it does |
|---|---|---|
| `POST` | `/api/auth/register` | Create an account |
| `POST` | `/api/auth/login` | Log in and get a token (valid for one hour) |
| `GET` | `/api/me` | Check who you're logged in as |
| `POST` | `/api/expenses` | Add an expense |
| `GET` | `/api/expenses?month=2024-03` | List your expenses for a month |
| `DELETE` | `/api/expenses/{id}` | Delete one of your expenses |
| `GET` | `/api/expenses/summary?month=2024-03` | Get the monthly summary |

Everything except register and login needs the token in the header: `Authorization: Bearer <token>`.

### Adding an expense

Here's a 10-dollar lunch:

```json
POST /api/expenses

{
  "description": "Lunch in New York",
  "amountCents": 1000,
  "currency": "USD",
  "spentOn": "2024-03-15",
  "category": "FOOD"
}
```

The API looks up the dollar-to-euro rate for March 15th, converts the amount, and saves both:

```json
{
  "id": "4d7def28-d128-4ebd-9016-5328957cc101",
  "description": "Lunch in New York",
  "amountCents": 1000,
  "currency": "USD",
  "amountEurCents": 918,
  "rateUsed": 0.91811,
  "spentOn": "2024-03-15",
  "category": "FOOD",
  "createdAt": "2026-11-09T15:21:12.063787Z"
}
```

### The monthly summary

```json
GET /api/expenses/summary?month=2024-03

{
  "month": "2024-03",
  "totalEurCents": 1836,
  "byCategory": {
    "FOOD": 1836
  },
  "ai": {
    "headline": "Total spending was only 18.36 EUR for the month of March",
    "unusualSpending": ["FOOD"],
    "savingTip": "Consider meal planning or packing lunches to reduce food expenses"
  }
}
```

If the AI isn't running, you still get all the numbers, just with `"ai": null`.

### When something goes wrong

Errors come back in the standard Problem Details format, and validation errors tell you exactly which fields to fix:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "errors": {
    "email": "must be email format",
    "password": "minimum size is 8"
  }
}
```

## Running it yourself

You'll need Java 21 or newer and PostgreSQL. Ollama is optional; without it, everything works except the AI part of the summary.

**1. Create the database**

```sql
CREATE DATABASE expense_tracker;
```

**2. Set two environment variables**

- `DB_PASSWORD`: your PostgreSQL password
- `JWT_SECRET`: a random string of at least 32 characters. `openssl rand -base64 48` gives you a good one.

That's all you need. If your setup differs from the defaults, you can also set `DATABASE_URL`, `DB_USER`, `LLM_BASE_URL`, `LLM_API_KEY` and `LLM_MODEL`. Check `application.properties` for the defaults.

A small tip from experience: make sure there's no space after the `=` when you set these. A leading space becomes part of the value, and Postgres will reject the password without telling you why.

**3. Get the AI model (optional)**

```bash
ollama pull llama3.2
```

**4. Start it**

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`. The API runs on `http://localhost:8080`.

To run the JWT tests: `./mvnw test -Dtest=JwtServiceTest`

## Why I built it this way

These are the decisions I spent the most time thinking about.

**Money is stored in cents, never as a `double`.**
Floating point numbers can't even represent 0.10 exactly, and those tiny errors add up. So every amount is a whole number of cents, and the currency conversion happens with `BigDecimal`. For rounding, I used banker's rounding (`HALF_EVEN`): instead of always rounding .5 up, it rounds to the nearest even number, so over many transactions the rounding doesn't slowly push totals upward. That's how banks do it.

**Every expense remembers the exact rate it was converted with.**
I could have looked the rate up again whenever needed, but storing it means anyone can check how an amount was calculated, nothing changes if historical data ever gets corrected, and listing expenses doesn't need a single API call. One thing I learned along the way: the European Central Bank only publishes rates on working days, so a Sunday expense gets Friday's rate.

**Java does the math, the AI just comments on it.**
Language models are good with words and unreliable with arithmetic. So all the totals are calculated in Java, and the AI only receives the finished numbers to write a headline and a tip. You can ask for the same summary five times: the wording changes, the numbers never do.

**The AI never sees your expense descriptions.**
It only gets totals and categories. That keeps personal details private, keeps the prompts small, and closes the door on prompt injection. Descriptions are text that users type, and someone could write "ignore your instructions and…" into one.

**If the AI fails, the summary still works.**
Ollama might be off, slow, or return messy output. In all those cases, you still get your numbers, just without the AI section, and the failure gets logged. I treated the exchange rate service differently on purpose: without a rate, an expense can't be saved, so a failure there is a real error (400 for an unknown currency, 502 if the service is down).

**You can't tell whether other people's data exists.**
Expenses are always looked up by their ID *and* your user ID together. If you try to delete someone else's expense, you get 404, not 403, because a 403 would confirm that the ID belongs to someone. Same idea at login: a wrong password and an unknown email give exactly the same error.

**No waiting on external services while holding the database.**
Exchange rate and AI calls can take seconds. If a database transaction stayed open during that time, it would block a connection the whole while. So external calls always happen outside transactions.

**The database schema is generated by Hibernate, which is fine for now, but not forever.**
This keeps a prototype simple, but it has limits. For example, Hibernate creates a database check for the allowed categories, and if I added a new category later, that check wouldn't update automatically. In a real project, I'd manage the schema with Flyway migrations instead.

## What I'd add next

- Flyway migrations, for the reason above
- Integration tests with Testcontainers, and mocked external APIs so tests don't depend on the internet
- Caching exchange rates, since the rate for a given day never changes
- Refresh tokens, so you don't have to log in again every hour
- Pagination for months with lots of expenses
