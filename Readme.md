# LWW Element Graph

> Conflict-Free Replicated Data Types (CRDTs) are data structures that power real-time collaborative applications
  in distributed systems. CRDTs can be replicated across systems, they can be updated independently and
  concurrently without coordination between the replicas, and it is always mathematically possible to resolve
  inconsistencies that might result.

The implementation provided in this repository tries to implement a `State Based LWW Element Set` using Graphs.

## Pre-requisite

- Java Software Development Kit (JDK) 11
- Maven 3.8.2

## Run with

```bash
> mvn clean install -Dcheckstyle.skip  
```