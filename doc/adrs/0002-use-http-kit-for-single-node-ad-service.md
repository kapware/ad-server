# 2. Use http-kit for single node ad service

Date: 2019-03-20

## Status

Accepted

## Context

After some discusion about the limitations and requirements for the ad server, it is still unclear what kind of constraints would shape the service like:
1. Traffic (including, mean number of requests, peak number of requests to be handled)
2. Quality and number of different ads
3. Acceptable costs
4. No concrete functional requirements/client criteria that need to be fulfilled.

## Decision

Therefore, it is been accepted that initial version would be an exploratory study fulfilling minimal criteria and lowest effort to have minimal viable product and, smallest amount of code, allowing rapid change and application of the constraints when they arise.
Http-kit seems to be perfect for that, as it supports more than enough 600k on a single-node setup, allowing flexibility (ring compatibility). With unknown ad criteria it is too early to design concrete database structure, so in addition to using http-kit it is been decided that the service would hold only in-mem mapping of ads->channels. 

## Consequences

The nature of the abvoe limitations might make the service not ready for production and not suitable for anything beyond veryfing assumptions and checking the overal feasibility of the idea. However it will be a good basis for implementing the actual service.
