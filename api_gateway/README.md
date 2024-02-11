# Butler API Gateway

Butler API Gateway is a gateway that provides a single point of entry for all the microservices in the Butler AI Chat App. It is a reverse proxy that routes requests to the appropriate microservice.

## Installation

## Usage

## Design Decisions

List different aspects of problems and their documentation references.

### Localization :earth_africa:

Localization on the server-side plays a big role of sending localized error message to the client and labels for the client to display.

#### Client ➡️ Server requests

Client requests data by sending a the requested language's code in the `Accept-Language` header and the labels' keys in body.

#### Client ⬅️ Server responses

The server should not include a detailed, localized error message in the response. Instead, it should include a code that the client can use to look up the error message in the client's language file. Reasoning: a certain endpoint can only have a certain set of error codes (at a particular API version). So the client already knows what type of error message there can be and may request it from the localization server if not cached. This way, the server does not have to send localized messages to the client.
