#/bin/sh
curl -H"application/githubpostreceive+json" -X POST http://localhost:8080/api/github -d @payload.json
