# Butler AI Chat App

Butler is an experimental AI chat app, which is a playground for testing out new technologies and development practices.

## Installation

### Backend Orchestration

Use Docker Compose to run the backend services.

```bash
# Build and run the backend services
docker-compose up
```

#### Monitoring

Grafana, Prometheus and Jaeger can be used to monitor the backend services.

### Android app

To build and run on the Android platform app, you should have a `local.properties` file in the project root directory (where the `composeApp`, `server` and `shared` folders are located) with the following content:

```properties
RELEASE_KEYSTORE_PASSWORD=your_release_keystore_password
RELEASE_KEY_PASSWORD=your_release_key_password
RELEASE_KEY_ALIAS=your_release_key_alias
RELEASE_KEY_PATH=path/to/release.keystore

DEBUG_KEYSTORE_PASSWORD=your_debug_keystore_password
DEBUG_KEY_PASSWORD=your_debug_key_password
DEBUG_KEY_ALIAS=your_debug_key_alias
DEBUG_KEY_PATH=path/to/debug.keystore
```

## Usage

## Contributing

See more information in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
