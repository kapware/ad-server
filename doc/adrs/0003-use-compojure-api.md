# 3. Use compojure-api

Date: 2019-03-25

## Status

Accepted

## Context

Compojure api comes with number of defaults for middle service, including, not limited to:
- common content types: application/json, application/transit
- spec support, including coercion
- swagger support
- ring/http-kit compatibility.

## Decision

The library is incorporated. 

## Consequences

Larger set of dependencies might be a problem in a longer run. This can be mitigated excluding unwanted dependencies.