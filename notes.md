TODO:

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

