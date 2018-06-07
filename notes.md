TODO:

PRIORITY
+ flex for past games on welome screen
+ minimum display time for shroud
  - quite glitchy now when the server is fast
  - write a par map2 and do the API call with a delay? <- blog
+ how to play
+ Write blog posts
  + overall design
    + inspiration
	+ architecture
  + API
    + Attempt
	+ tests
  + frontend
    + elm basics
	+ CSS framework
  + infrastructure
    + lambda considerations
	+ cloudformation
+ favicon (and updated manifest etc)

UI
+ consider skipping "loading backend" message
+ screen to show full standings (with words)
+ Say on spectating page what the currently active role is?
  + and who has that role?
+ Stanley pictures on loading screens

GENERAL
+ Show joined players on CreatorWaiting screen
+ better asset versioning (cache headers) so updates manifest?
* error handling (minimise usage of generic error view)
+ UI tests that ensure user journey
+ manage game screen for creator
  - manage buyer
  - kick players
  - allow player to join after start
+ use flags for api endpoint config
+ stop being buyer without awarding point?
* display fixes
  + discarded words
* change model
* frontend tests
  * https://medium.com/@_rchaves_/testing-in-elm-93ad05ee1832
* help pages

BUILD / OVERHEAD
+ cloudformation for build pipeline
+ refactor backend into sub-project

LONG TERM
+ refactor frontend to use navigation / SPA style
  * https://dev.to/rtfeldman/tour-of-an-open-source-elm-spa


DONE

UI
- Fix Welcome loading
  - skip "loading" or make it nicer
- icon on join game button
- 2x3 grid of words on spectate page
- breadcrumb nav
- use form labels (instead of just placeholders)
  - materialize style animated to show both?
- smaller headers
  - spectate header especially is bad
+ integrate error handling

BUILD
- switch to elm-live instead of elm-reactor with hot reloading for frontend dev server (still use something for local API)
- PROD-ready
  - CloudFront
  - static assets in a bucket
  - domain and cert
- codepipeline / codebuild / codedeploy CI


GENERAL
- Rejoin doesn't work for buyer!
- non-lifecycle errors are sticky
- additional navigation (e.g. leave game / rejoin from welcome)
  * (see also "breadcrumbs, above)
* improve error messages for users
- error messages
- errors as well (see above)
- restart game from saved state (e.g. local storage) on welcome screen?
  * https://stackoverflow.com/questions/33697444/data-persistence-in-elm
- rejoin games
- otherPlayers in API should only return *other players*
* display fixes
  - mobile zoom
  - show own name
  - show game name
  - unclickable self
  - display own points
  - display other players' points
* flow fixes
  + back from create/join
  + back from bad game code (failed join)
* flow fixes
- styles (materialize)
* cannot become buyer if someone else is?
* dev server with assets
* varying poll interval (depending on lifecycle)
* better join mechanic
  + prefix support in API
  + find unique prefix in API ( -> model)
  + prefixed game key display
- refactor views
- join game, password? (if required)
- loading pages over last page
  - wipe a curtain with a stanley picture or something
- design UI as a game
- loading pages use vdom!
  - starts with same headings etc as target page, spinner that loads content. Swaps content in when ready.


* elm SPA frontend
  - create / join
    + create takes password
	+ QR code to join? Or tapphones with Android beam?
	+ join scans QR code, inputs code?
  - joiners put their screenname in
  - creator arranges joined people in order
  - configures game (e.g. time limits)
  - creator 'starts' which deals cards into the game state
  - hits lambda backend for application state reads / updates

* S3 state
  - /prefix/<gameid>/game.json (GameState)
  - /prefix/<gameid>/<playerkey>.json (PlayerState)
  - stores JSON blob of game state, which contains player keys to look up player data
  - there's a race condition on registering :-/
    + Could write players only when the game starts, using ls? <- this sounds sensible
  - players, player cards, player scores (and won cards), current product
  - also player's previous cards so they aren't re-dealt
  - only things that affect other player's rows are awarding win and passing the pitch I think?

* Lambda backend
 - hard-coded list of roles and words as jar resource?
 - lambda randomly deals out of this


Ensure:
* ALL data auto-deleted after a couple of hours?

