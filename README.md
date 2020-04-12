# Quack Stanley

Quack Stanley is an
[Apples-to-Apples](https://boardgamegeek.com/boardgame/74/apples-apples)-style
party game, inspired by
[Snake Oil](https://boardgamegeek.com/boardgame/113289/snake-oil),
designed by
[Jeff Ochs](https://boardgamegeek.com/boardgame/113289/snake-oil/credits).

Play at [quackstanley.net](https://quackstanley.net), have fun!

## Codebase

Quack Stanley is an [Elm](http://elm-lang.org/) frontend with a
[Scala](https://www.scala-lang.org/) backend that runs "serverless"
as an [AWS Lambda Function](https://aws.amazon.com/lambda/).

### Running locally

To run Quack Stanley locally, you will need the following installed:

* [A scala-compatible JDK](https://docs.scala-lang.org/overviews/jdk-compatibility/overview.html)
* [sbt](https://www.scala-sbt.org/)
* [create-elm-app](https://github.com/halfzebra/create-elm-app)

With those installed, you can run the api and frontend using the helper
scripts in the root of the repository.

* devapi.sh
* devfrontend.sh

When run in development mode the game data is stored in-memory and will not
persist through a restart of the API.

### Running in AWS

Cloudformation templates are provided for running Quack Stanley in AWS.

**Notes:**
- Running Quack Stanley in AWS will incur costs.
- Many properties are hard-coded for the public Quack Stanley
service. If you'd like to change these to run your own version of Quack Stanley
some settings will need to be changed.

#### Quack Stanley storage

[`quack-stanley-storage.template.yaml`](cloudformation/quack-stanley-storage.template.yaml)
will create the persistent resources needed to run Quack Stanley.
Quack Stanley stores its data in S3 so the template creates S3 an bucket
for the web assets and another for storing the data for in-progress games.

#### Quack Stanley

[`quack-stanley.template.yaml`](cloudformation/quack-stanley.template.yaml)
sets up the application itself. It creates an API Gateway with an AWS Lambda
function as the backend, and a CloudFront distribution for serving the webapp.
It also uses Route53 to configure the webapp's DNS.
