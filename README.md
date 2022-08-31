# Dofus API
[![Build](https://github.com/dofusdude/dofus-api/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/dofusdude/dofus-api/actions/workflows/maven.yml)

A multilingual API for the Dofus encyclopedia.

## THE PROJECT IS DEPRECATED AND WILL NEVER GET ANY FEATURE OR DATA UPDATES. USE [THIS API](https://dofusdu.de) INSTEAD.
The instance and this repository will stay online for others to help building their own API.

But I advice using the linked new project since it has much more features and the encyclopedia is heavily unreliable and missing languages.

## Usage
See the OpenAPI or SwaggerUI endpoint for types and examples:
- https://enc.dofusdu.de/swagger
- https://enc.dofusdu.de/openapi

## Running your own instance
If you want persistent data, uncomment the volume line in docker-compose.yml. After that, do
```shell script
docker-compose up -d
```

## Adding items
The Dofus encyclopedia isn't always up-to-date. A fix for that is already planned for later versions.
For the moment being the API can only be changed with the API key.

## Awesome projects using the API
- [Craftlist](https://dofus-craftlist.netlify.app) by Lystina

## Development
```shell script
docker-compose -f docker-compose.dev.yml up -d
./mvnw compile quarkus:dev
```
If that does not work (maybe because you are on Windows), download Maven for yourself and enter `mvn quarkus:dev` in the
project directory.

Note: The API is build on Quarkus. https://quarkus.io for more.

## License
Author: Christopher Sieh <stelzo@steado.de>

This project is licensed under the Apache-2.0 License.
