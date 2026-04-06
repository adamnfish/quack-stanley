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

#### Structure

| Project                           | Purpose                                 |
|-----------------------------------|-----------------------------------------|
| [core](/core)                     | Quack Stanley game logic                |
| [lambda](/lambda)                 | AWS Lambda handler                      |
| [frontend](/frontend)             | Web application for game UI             |
| [cloudformation](/cloudformation) | Infrastructure for AWS deployment       |
| [dev-server](/dev-server)         | Standalone server for local development |
| [e2e](/e2e)                       | Playwright end-to-end tests             |

### Running locally

To run Quack Stanley locally, you will need the following installed:

* [A scala-compatible JDK](https://docs.scala-lang.org/overviews/jdk-compatibility/overview.html)
* [sbt](https://www.scala-sbt.org/)
* [node and npm](https://nodejs.org/)

Install all npm dependencies from the repository root (this covers both the
frontend and e2e workspaces):

```
npm install
```

You can then run the api and frontend using the helper scripts in the
root of the repository.

* devapi.sh
* devfrontend.sh

The development server stores game data in-memory. This data will not
persist through a restart of the API.

### Running E2E tests

The end-to-end tests exercise a complete three-player game session, capturing
screenshots at each step across multiple device sizes.

Prerequisites: the API dev server must be running, and the frontend must be built.

```
./devapi.sh &         # start the Scala dev server in the background
npm run build:frontend
npm run e2e
```

To view the test report with all screenshots:

```
npm run e2e:report
```

The report is also uploaded as a `playwright-report` artifact on every CI run.

### Running in AWS

Quack Stanley can be run in AWS using the two CloudFormation templates provided
in this repository.

**Notes:**
- Running Quack Stanley in AWS will incur costs.
- Some properties are hard-coded for the public Quack Stanley
service. If you'd like to change these to run your own version of Quack Stanley
some settings will need to be changed.
- The region is currently hard-coded in Quack Stanley's source code

#### Quack Stanley storage

[`quack-stanley-storage.template.yaml`](cloudformation/quack-stanley-storage.template.yaml)
creates the persistent resources needed to run Quack Stanley.
Quack Stanley stores its data in S3. The template creates one S3 bucket
for the web assets and another for storing the data for in-progress games.

#### Quack Stanley

[`quack-stanley.template.yaml`](cloudformation/quack-stanley.template.yaml)
sets up the application itself. It creates an API Gateway with an AWS Lambda
function as the backend, and a CloudFront distribution for serving the webapp.
It also uses Route53 to configure the webapp's DNS.
