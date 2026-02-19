# CV Processing API

A Spring Boot REST API that accepts CV file uploads, uses Google Gemini to extract structured fields, and validates them against predefined rules.

---

## Prerequisites

- Java 21+
- Maven
- A Gemini API key — get one free from [Google AI Studio](https://aistudio.google.com/api-keys) (no billing required)

---

## Running the application

**1. Get a free Gemini API key** from [Google AI Studio](https://aistudio.google.com/api-keys).
Sign in with a Google account, click **Create API key**, and copy it. Takes about 30 seconds, no billing required.

**2. Set it as an environment variable:**

```bash
export GEMINI_API_KEY=your_api_key_here
```

If running from IntelliJ: **Run > Edit Configurations > Environment variables**, then add `GEMINI_API_KEY=your_api_key_here`.

**3. Start the application:**

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Testing via Swagger UI

Open `http://localhost:8080/swagger-ui/index.html` in your browser.

1. Click `POST /api/cv/analyze`
2. Click **Try it out**
3. Upload a PDF or DOCX file
4. Click **Execute**

---

## API

### `POST /api/cv/analyze`

**Request:** `multipart/form-data` with a `file` field (PDF or DOCX).

**Response:**

```json
{
  "extracted": {
    "workExperienceYears": 1.5,
    "skills": [
      "Java",
      "Spring Boot",
      "LLMs"
    ],
    "languages": [
      "Hungarian",
      "English"
    ],
    "profile": "Enthusiastic developer with experience in..."
  },
  "validations": [
    {
      "field": "workExperience",
      "passed": true,
      "reason": "1.5 years is within the 0–2 year range"
    },
    {
      "field": "skills",
      "passed": true,
      "reason": "Skills include both Java and LLMs"
    },
    {
      "field": "languages",
      "passed": true,
      "reason": "Languages include both Hungarian and English"
    },
    {
      "field": "profile",
      "passed": true,
      "reason": "Expresses interest in both GenAI and Java"
    }
  ]
}
```

The endpoint always returns `200 OK`. Validation failures are reflected in the `passed` field of each validation result, not in the HTTP status code. A `400` is returned only if the uploaded file cannot be parsed.

---

## Running the integration tests

Set the `GEMINI_API_KEY` environment variable as above (also set it in IntelliJ's test run configuration if running from the IDE), then:

```bash
./mvnw test
```

The tests make real Gemini API calls and verify the full pipeline end-to-end against two sample CVs in `src/test/resources/`.

> Note: the free tier has a rate limit of 20 requests per minute. If tests fail with a 429 error, wait a minute and re-run.

---

## Design decisions

**Document parsing — Apache Tika**
Tika handles PDF and DOCX with a single unified API, auto-detecting the file format. The alternative was using separate libraries per format (e.g. PDFBox for PDF, Apache POI for DOCX), which would require format detection logic and two different code paths to maintain. Tika eliminates that entirely.

**Single LLM call for all four fields**
All four fields are extracted in one prompt rather than one call per field. This keeps latency low and avoids burning through rate limit quota unnecessarily. The trade-off is a more complex prompt, but the structured output via `BeanOutputConverter` keeps the response schema strict and the result typed.

**Prompt normalization**
LLMs are inconsistent about how they represent the same concept — a CV might say "Generative AI", "prompt engineering", or "ChatGPT", and languages often come with qualifiers like "English (C1)". The extraction prompt explicitly normalizes these: LLM-related skills are collapsed into `"LLMs"`, and language names are stripped of proficiency qualifiers. This makes downstream Java validation reliable without adding fuzzy matching logic.

**Model choice — `gemini-2.5-flash`**
Flash models are optimized for speed and cost over raw capability. For structured extraction and a simple semantic check, a reasoning-heavy model would be overkill. `gemini-2.5-flash` handles both tasks accurately at a fraction of the cost and latency of larger models.

**Validation — Java for deterministic rules, LLM for semantic rules**

| Field           | Approach | Reason                                                  |
|-----------------|----------|---------------------------------------------------------|
| Work experience | Java     | Numeric range check — deterministic, instant, free      |
| Skills          | Java     | List membership check — deterministic, instant, free    |
| Languages       | Java     | List membership check — deterministic, instant, free    |
| Profile         | Gemini   | Semantic intent check — requires language understanding |

Using an LLM only where genuinely necessary keeps the system fast, cost-efficient, and predictable. The profile validation is the only rule that cannot be expressed as a simple Java predicate.

**Error handling strategy**
The endpoint returns `200 OK` even when validations fail, because a failed validation is a valid and expected outcome — not an error. A `400` is reserved for the case where the uploaded file cannot be parsed at all, which is a bad request. This keeps the HTTP semantics clean and avoids forcing clients to treat business logic failures as errors.

**Java records over Lombok**
Model classes use Java records (`CvData`, `ValidationResult`, `CvAnalysisResponse`) rather than Lombok-annotated classes. Records are immutable by design, require no annotation processing, and express intent clearly — these are pure data carriers with no behaviour. Lombok would add a compile-time dependency to accomplish the same thing less explicitly.

**Constructor injection over field injection**
All services use constructor injection rather than `@Autowired` field injection. This makes dependencies explicit, allows the classes to be instantiated and tested without a Spring context, and avoids the hidden coupling that field injection creates.