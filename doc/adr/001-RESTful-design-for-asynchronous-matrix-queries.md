# ADR-001: Asynchronous matrix query API design (REST-inspired)

Date: 2026-01-01 | Status: Accepted


## Problem

The API must support execution of computationally intensive matrix queries that cannot be processed synchronously. Clients need a reliable way to:

* Submit a query
* Check its processing status
* Retrieve results once available

Additionally:

* Listing all queries (`GET /matrix/queries`) is currently not supported but may be introduced in the future when authentication and user scoping are available
* The API should remain as **RESTful and predictable as possible**

Several designs were considered internally. No external examples or prior art were adopted directly. Alternative designs were considered internally. These approaches introduced ambiguity in resource modeling and deviated from REST conventions.


## Decision

Adopt a **resource-oriented asynchronous pattern**:

```http
POST /matrix/queries
GET  /matrix/queries/{queryId}
GET  /matrix/queries/{queryId}/result
```

Behavior:

* `POST /matrix/queries`

    * Creates a new query job
    * Returns `202 Accepted`
    * Includes `Location` header pointing to `/matrix/queries/{queryId}`

* `GET /matrix/queries/{queryId}`

    * `200 OK` → returns the query job resource with status (`Processing`, `Completed`, etc.)
    * `404 Not Found` → unknown

* `GET /matrix/queries/{queryId}/result`

    * `200 OK` → returns result
    * `202 Accepted` → still processing
    * `404 Not Found` → unknown or already consumed
    * Optionally `410 Gone` → explicitly indicates expired/consumed result


## Rationale

There is **no strictly RESTful way** to model this use case due to its asynchronous nature. This design therefore follows a **REST-inspired compromise**, using resource-oriented modeling and HTTP semantics where possible.

A resource-oriented design is used to provide a clear structure and alignment with HTTP semantics.

The query is modeled as a **resource (a job)** rather than an action. This aligns with REST principles where:

* URLs represent **nouns (resources)**, not verbs
* State is retrieved via `GET`, not encoded in endpoint names

This avoids RPC-style patterns like:

```http
GET /matrix/query/status?queryId=...
```

which treat the API as a function call instead of a resource system.


### Proper use of path vs query parameters

* `queryId` is a **resource identifier**, not a filter
* Therefore it belongs in the **path**, not as a query parameter

Correct:

```http
GET /matrix/queries/{queryId}
```

Not recommended:

```http
GET /matrix/query/status?queryId=...
```


### Asynchronous HTTP semantics

The design follows established HTTP patterns:

* `202 Accepted` for async processing
* `Location` header to the created resource
* Polling via `GET` on the resource

This makes the API intuitive for clients and compatible with standard HTTP tooling.


### No requirement for collection listing

Although `POST /matrix/queries` implies a collection, REST does **not require** implementing:

```http
GET /matrix/queries
```

The API can support creation without exposing listing, which fits system constraints.


### Result lifecycle

The lifecycle of query results is not strictly defined in this design and may evolve. Results may be retained for a limited time and cleaned up asynchronously based on system policies (e.g., time-based expiration).

Clients should not assume results are stored indefinitely and should retrieve them in a timely manner.

The API may return:

* `200 OK` → result available
* `202 Accepted` → still processing
* `404 Not Found` or `410 Gone` → result no longer available


### Alternatives considered

An action-oriented (RPC-style) design was considered but not chosen. It models operations as actions rather than resources, which makes the API less consistent and harder to evolve. A resource-oriented design provides a clearer structure and aligns better with HTTP semantics.


## Consequences

### Positive:

* Clear, REST-inspired, resource-oriented design
* Aligns with standard HTTP async patterns (`202`, `Location`)
* Scales well for future extensions (e.g., cancellation, metadata)
* Predictable and easy for clients to understand

### Negative:

* Requires client-side polling logic
* Slightly more endpoints than minimal RPC-style design

### Neutral:

* Optional use of `410 Gone` vs `404 Not Found` depends on implementation preference
