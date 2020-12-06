const {webkit} = require('playwright');
const fetch = require('node-fetch');
const path = require('path');
const fs = require('fs');
const PNG = require('pngjs').PNG;
const pixelmatch = require('pixelmatch');


(async () => {
  const appUrl = 'http://localhost:3000/';
  const apiUrl = 'http://localhost:9001/api';
  const browser = await webkit.launch({headless: true, acceptDownloads: true});

  try {
    await normalGame(appUrl, browser);
  } catch(err) {
    console.log("Error in normal game flow", err);
  }
  try {
    await externalGame(appUrl, apiUrl, browser);
  } catch(err) {
    console.log("Error in external game flow", err);
  }

  process.exit(0);
})();


async function normalGame(appUrl, browser) {
  // needs to be > than the ping interval (15000)
  const timeout = 16000;

  const hostContext = await browser.newContext();
  const host = await hostContext.newPage();
  await host.setViewportSize({width: 360, height: 1500});
  await host.setDefaultTimeout(timeout);

  const player1Context = await browser.newContext();
  const player1 = await player1Context.newPage();
  await player1.setViewportSize({width: 601, height: 900});
  await player1.setDefaultTimeout(timeout);

  const player2Context = await browser.newContext();
  const player2 = await player2Context.newPage();
  await player2.setViewportSize({width: 1920, height: 1000});
  await player2.setDefaultTimeout(timeout);


  // host creates game
  console.log("game creation");
  await host.goto(appUrl);
  await host.waitForSelector(lifecycleHook('welcome'));

  await screenshot(host, 'host', 'a01-welcome.png');

  await host.click(buttonWithText('Create game'));
  await host.waitForSelector(lifecycleHook('create'));

  await screenshot(host, 'host', 'a02-1-create-game.png');
  await host.fill(textInput('Game name'), "Test game");
  await host.fill(textInput('Player name'), "Host");
  await screenshot(host, 'host', 'a02-2-create-game-with-input.png');

  await host.click(buttonWithText('Create game'));

  await host.waitForSelector(lifecycleHook('host-waiting'));

  await screenshot(host, 'host', 'a03-1-lobby.png');
  const gameCode = await host.$eval("css=#game-code", codeEl => codeEl.value);


  // player 1 joins game
  console.log("player 1 joining");
  await player1.goto(appUrl);
  await player1.waitForSelector(lifecycleHook('welcome'));

  await screenshot(player1, 'player1', 'a01-welcome.png');

  await player1.click(buttonWithText('Join game'));
  await player1.waitForSelector(lifecycleHook('join'));

  await screenshot(player1, 'player1', 'a02-1-join-game.png');
  await player1.fill(textInput('Game code'), gameCode);
  await player1.fill(textInput('Player name'), "Player 1");
  await screenshot(player1, 'player1', 'a02-2-join-game-with-input.png');

  await player1.click(buttonWithText('Join game'));
  await player1.waitForSelector(lifecycleHook('waiting'));

  await screenshot(player1, 'player1', 'a03-1-lobby.png');

  // wait for player 1 to appear on host's screen
  await host.waitForSelector('text="Player 1"');
  await screenshot(host, 'host', 'a03-2-lobby.png');


  // player 2 joins game
  console.log("player 2 joining");
  await player2.goto(appUrl);
  await player2.waitForSelector(lifecycleHook('welcome'));

  await screenshot(player2, 'player2', 'a01-welcome.png');

  await player2.click(buttonWithText('Join game'));
  await player2.waitForSelector(lifecycleHook('join'));

  await screenshot(player2, 'player2', 'a02-1-join-game.png');
  await player2.fill(textInput('Game code'), gameCode);
  await player2.fill(textInput('Player name'), "Player 2");
  await screenshot(player2, 'player2', 'a02-2-join-game-with-input.png');

  await player2.click(buttonWithText('Join game'));
  await player2.waitForSelector(lifecycleHook('waiting'));

  await screenshot(player2, 'player2', 'a03-1-lobby.png');

  // wait for player 2 to appear on host's screen
  await host.waitForSelector('text="Player 2"');
  await screenshot(host, 'host', 'a03-3-lobby.png');


  // host starts game
  console.log("starting game");
  await host.click(buttonWithText('Start game'));
  await host.waitForSelector(lifecycleHook('spectating'));

  await screenshot(host, 'host', 'a04-1-spectating-screen.png');

  await player1.waitForSelector(lifecycleHook('spectating'));
  await screenshot(player1, 'player1', 'a04-1-spectating-screen.png');

  await player2.waitForSelector(lifecycleHook('spectating'));
  await screenshot(player2, 'player2', 'a04-1-spectating-screen.png');


  // player 1 becomes buyer
  console.log("player 1 becoming buyer");
  await player1.click(buttonWithText("Buyer"));
  await player1.waitForSelector(lifecycleHook("buying"));
  await screenshot(player1, 'player1', 'a05-1-buying.png');


  // host pitches to player 1
  console.log("host pitches to buyer");
  const hostWords = await host.$$eval('css=.hand__container button', buttons => buttons.map(b => b.innerText));
  await host.click(buttonWithText(hostWords[0]));
  await host.click(buttonWithText(hostWords[1]));
  await screenshot(host, 'host', 'a05-1-selecting-words.png');

  await host.click(buttonWithText("Start pitch"));
  await host.waitForSelector(lifecycleHook("pitching"));
  await screenshot(host, 'host', 'a05-2-pitching.png');

  await host.click(buttonWithText("Finish pitch"));
  await host.waitForSelector(lifecycleHook("spectating"));
  await screenshot(host, 'host', 'a05-3-finished-pitching.png');

  await player1.waitForSelector('text="' + hostWords[0].toLowerCase() + ' ' + hostWords[1].toLowerCase() + '"');
  await screenshot(player1, 'player1', 'a05-2-buying.png');


  // player 2 pitches to player 1
  console.log("player 2 pitches to buyer");
  const p2Words = await player2.$$eval('css=.hand__container button', buttons => buttons.map(b => b.innerText));
  await player2.click(buttonWithText(p2Words[0]));
  await player2.click(buttonWithText(p2Words[1]));
  await screenshot(player2, 'player2', 'a05-1-selecting-words.png');

  await player2.click(buttonWithText("Start pitch"));
  await player2.waitForSelector(lifecycleHook("pitching"));
  await screenshot(player2, 'player2', 'a05-2-pitching.png');

  await player2.click(buttonWithText("Finish pitch"));
  await player2.waitForSelector(lifecycleHook("spectating"));
  await screenshot(player2, 'player2', 'a05-3-finished-pitching.png');

  await player1.waitForSelector('text="' + p2Words[0].toLowerCase() + ' ' + p2Words[1].toLowerCase() + '"');
  await screenshot(player1, 'player1', 'a05-3-buying.png');

  // player 1 selects player 2 as winner
  console.log("selecting winner");
  await player1.click(buttonWithText(p2Words[0] + ' ' + p2Words[1]));
  await player1.waitForSelector(lifecycleHook("spectating"));
  await screenshot(player1, 'player1', 'a05-4-finished-buying.png');

  // check scores
  console.log("checking scores");
  await screenshot(host, 'host', 'a05-3.5-point-awarded.png');
  await host.waitForSelector('xpath=//*[contains(@class, "point") and contains(text(), "1")]');
  await screenshot(host, 'host', 'a05-4-point-awarded.png');
  await player2.waitForSelector('xpath=//*[contains(@class, "point") and contains(text(), "1")]');
  await screenshot(player2, 'player2', 'a05-4-point-awarded.png');

  console.log("done");
}

async function externalGame(appUrl, apiUrl, browser) {
  // needs to be > than the ping interval (15000)
  const timeout = 16000;

  const hostContext = await browser.newContext();
  const host = await hostContext.newPage();
  await host.setViewportSize({width: 360, height: 1500});
  await host.setDefaultTimeout(timeout);

  const player1Context = await browser.newContext();
  const player1 = await player1Context.newPage();
  await player1.setViewportSize({width: 601, height: 900});
  await player1.setDefaultTimeout(timeout);

  const player2Context = await browser.newContext();
  const player2 = await player2Context.newPage();
  await player2.setViewportSize({width: 1920, height: 1000});
  await player2.setDefaultTimeout(timeout);

  // setup game
  console.log("setting up external game");
  const response = await fetch(apiUrl, {
    method: 'post',
    body: JSON.stringify({
        operation: "setup-game",
        gameName: "Test game"
    }),
    headers: {'Content-Type': 'application/json'}
  });
  const emptyGameData = await response.json();

  // host creates game
  console.log("host joining");
  await host.goto(appUrl + "?gameCode=" + emptyGameData.gameCode + "&hostCode=" + emptyGameData.hostCode + "&name=Host");
  await host.waitForSelector(lifecycleHook('join'));

  await screenshot(host, 'host', 'b01-pre-filled-join.png');

  await host.click(buttonWithText('Join game'));
  await host.waitForSelector(lifecycleHook('host-waiting'));

  await screenshot(host, 'host', 'b03-1-lobby.png');
  const gameCode = await host.$eval("css=#game-code", codeEl => codeEl.value);

  // player 1 joins game
  console.log("player 1 joining");
  await player1.goto(appUrl + "?gameCode=" + emptyGameData.gameCode + "&name=Player%201");
  await player1.waitForSelector(lifecycleHook('join'));

  await screenshot(player1, 'player1', 'b01-pre-filled-join.png');

  await player1.click(buttonWithText('Join game'));
  await player1.waitForSelector(lifecycleHook('waiting'));

  await screenshot(player1, 'player1', 'b03-1-lobby.png');

  // wait for player 1 to appear on host's screen
  await host.waitForSelector('text="Player 1"');
  await screenshot(host, 'host', 'b03-2-lobby.png');


  // player 2 joins game
  console.log("player 2 joining");
  await player2.goto(appUrl + "?gameCode=" + emptyGameData.gameCode + "&name=Player%202");
  await player2.waitForSelector(lifecycleHook('join'));

  await screenshot(player2, 'player2', 'b01-pre-filled-join.png');

  await player2.click(buttonWithText('Join game'));
  await player2.waitForSelector(lifecycleHook('waiting'));

  await screenshot(player2, 'player2', 'b03-1-lobby.png');

  // wait for player 2 to appear on host's screen
  await host.waitForSelector('text="Player 2"');
  await screenshot(host, 'host', 'b03-3-lobby.png');


  // host starts game
  console.log("starting game");
  await host.click(buttonWithText('Start game'));
  await host.waitForSelector(lifecycleHook('spectating'));

  await screenshot(host, 'host', 'b04-1-spectating-screen.png');

  await player1.waitForSelector(lifecycleHook('spectating'));
  await screenshot(player1, 'player1', 'b04-1-spectating-screen.png');

  await player2.waitForSelector(lifecycleHook('spectating'));
  await screenshot(player2, 'player2', 'b04-1-spectating-screen.png');
};


// Utilities

async function screenshot(player, playerName, fileName) {
  // delay to allow transitions to complete
  await sleep(501);
  await player.screenshot({path: screenshotPath(playerName, fileName)});

  // generate diff
  fs.mkdirSync(path.dirname(screenshotDiffPath(playerName, fileName)), {recursive: true});
  const img1 = PNG.sync.read(fs.readFileSync(screenshotPath(playerName, fileName)));
  const img2 = PNG.sync.read(fs.readFileSync(screenshotReferencePath(playerName, fileName)));
  const {width, height} = img1;
  const diff = new PNG({width, height});
  const count = pixelmatch(img1.data, img2.data, diff.data, width, height, {threshold: 0.1});
  fs.writeFileSync(screenshotDiffPath(playerName, fileName), PNG.sync.write(diff));
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function lifecycleHook(lifecycleName) {
  return 'css=.container.' + lifecycleName;
}

function buttonWithText(text) {
  return "css=button >> text=" + text + "";
}

function textInput(label) {
  return "xpath=//label[contains(text(), '" + label + "')]/..//input";
}

/**
 * WAT screenshots are written here
 */
function screenshotPath(player, filename) {
  return 'screenshots/latest/' + player + '/' + filename;
}

/**
 * Reference screenshots are in version control
 */
function screenshotReferencePath(player, filename) {
  return 'screenshots/reference/' + player + '/' + filename;
}

/**
 * Diffs represent regressions or changes, noise aside
 */
function screenshotDiffPath(player, filename) {
  return 'screenshots/diff/' + player + '/' + filename;
}
