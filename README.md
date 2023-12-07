# Butler AI Chat App

Butler is an AI chat app, which covers all fronts of development, from frontend to backend, and from design to deployment.

## Installation

## Usage

### Client

Butler Client can be build with [Android Studio](https://developer.android.com/studio) via [Gradle](https://gradle.org/).

All available binaries and .apk files can be found in the [latest release](https://github.com/HLCaptain/butler/releases/latest).

### Server

The server's job is to run Large Language Model inference for incoming messages. It is written in Python and uses the Firebase Firestore database to listen for new messages. It relies on [OpenLLM](https://github.com/bentoml/OpenLLM) to run inference.

The server must be run on Linux, as OpenLLM only supports Linux.

## Contributing

See more information in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
