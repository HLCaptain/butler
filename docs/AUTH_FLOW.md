# Butler Authentication Flow

Butler relies on JWT tokens to get access to resources. The authentication flow is as follows:

1. The user's credentials are sent to the Butler server. Passwords are hashed on client side before sending them to the server.
2. The server validates the credentials and sends back a JWT token for a particular user. Password is hashed again on the server side before checking it with the value in the database with Argon2 algorithm.
3. Any error during authentication flow results in a "User not found" type error.
