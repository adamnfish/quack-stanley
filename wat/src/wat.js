const {webkit} = require('playwright');

(async () => {
  const appUrl = 'http://localhost:3000/';
  const browser = await webkit.launch({headless: true, acceptDownloads: true});

  try {
    await normalGame(appUrl, browser);
  } catch(err) {
    console.log(err);
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
  await player1.setViewportSize({width: 601, height: 1500});
  await player1.setDefaultTimeout(timeout);

  const player2Context = await browser.newContext();
  const player2 = await player2Context.newPage();
  await player2.setViewportSize({width: 1920, height: 1500});
  await player2.setDefaultTimeout(timeout);


  // host creates game
  console.log("game creation");
  await host.goto(appUrl);
  await host.waitForSelector(lifecycleHook('welcome'));

  await screenshot(host, 'host', '01-welcome.png');

  await host.click(buttonWithText('Create game'));
  await host.waitForSelector(lifecycleHook('create'));

  await screenshot(host, 'host', '02-1-create-game.png');
  await host.fill(textInput('Game name'), "Test game");
  await host.fill(textInput('Player name'), "Host");
  await screenshot(host, 'host', '02-2-create-game-with-input.png');

  await host.click(buttonWithText('Create game'));

  await host.waitForSelector(lifecycleHook('host-waiting'));

  await screenshot(host, 'host', '03-1-lobby.png');
  const gameCode = await host.$eval("css=#game-code", codeEl => codeEl.value);


  // player 1 joins game
  console.log("player 1 joining");
  await player1.goto(appUrl);
  await player1.waitForSelector(lifecycleHook('welcome'));

  await screenshot(player1, 'player1', '01-welcome.png');

  await player1.click(buttonWithText('Join game'));
  await player1.waitForSelector(lifecycleHook('join'));

  await screenshot(player1, 'player1', '02-1-join-game.png');
  await player1.fill(textInput('Game code'), gameCode);
  await player1.fill(textInput('Player name'), "Player 1");
  await screenshot(player1, 'player1', '02-2-join-game-with-input.png');

  await player1.click(buttonWithText('Join game'));
  await screenshot(player1, 'player1', 'xx-println.png');
  await player1.waitForSelector(lifecycleHook('waiting'));

  await screenshot(player1, 'player1', '03-1-lobby.png');

  // wait for player 1 to appear on host's screen
  await host.waitForSelector('text="Player 1"');
  await screenshot(host, 'host', '03-2-lobby.png');


  // player 2 joins game
  console.log("player 2 joining");
  await player2.goto(appUrl);
  await player2.waitForSelector(lifecycleHook('welcome'));

  await screenshot(player2, 'player2', '01-welcome.png');

  await player2.click(buttonWithText('Join game'));
  await player2.waitForSelector(lifecycleHook('join'));

  await screenshot(player2, 'player2', '02-1-join-game.png');
  await player2.fill(textInput('Game code'), gameCode);
  await player2.fill(textInput('Player name'), "Player 2");
  await screenshot(player2, 'player2', '02-2-join-game-with-input.png');

  await player2.click(buttonWithText('Join game'));
  await player2.waitForSelector(lifecycleHook('waiting'));

  await screenshot(player2, 'player2', '03-1-lobby.png');

  // wait for player 2 to appear on host's screen
  await host.waitForSelector('text="Player 2"');
  await screenshot(host, 'host', '03-3-lobby.png');


  // host starts game
  console.log("starting game");
  await host.click(buttonWithText('Start game'));
  await host.waitForSelector(lifecycleHook('spectating'));

  await screenshot(host, 'host', '04-1-spectating-screen.png');

  await player1.waitForSelector(lifecycleHook('spectating'));
  await screenshot(player1, 'player1', '04-1-spectating-screen.png');

  await player2.waitForSelector(lifecycleHook('spectating'));
  await screenshot(player2, 'player2', '04-1-spectating-screen.png');


  // player 1 becomes buyer
  console.log("player 1 becoming buyer");
  await player1.click(buttonWithText("Buyer"));
  await player1.waitForSelector(lifecycleHook("buying"));
  await screenshot(player1, 'player1', '05-1-buying.png');


  // host pitches to player 1
  console.log("host pitches to buyer");
  const hostWords = await host.$$eval('css=.hand__container button', buttons => buttons.map(b => b.innerText));
  await host.click(buttonWithText(hostWords[0]));
  await host.click(buttonWithText(hostWords[1]));
  await screenshot(host, 'host', '05-1-selecting-words.png');

  await host.click(buttonWithText("Start pitch"));
  await host.waitForSelector(lifecycleHook("pitching"));
  await screenshot(host, 'host', '05-2-pitching.png');

  await host.click(buttonWithText("Finish pitch"));
  await host.waitForSelector(lifecycleHook("spectating"));
  await screenshot(host, 'host', '05-3-finished-pitching.png');

  await player1.waitForSelector('text="' + hostWords[0].toLowerCase() + ' ' + hostWords[1].toLowerCase() + '"');
  await screenshot(player1, 'player1', '05-2-buying.png');


  // player 2 pitches to player 1
  console.log("player 2 pitches to buyer");
  const p2Words = await player2.$$eval('css=.hand__container button', buttons => buttons.map(b => b.innerText));
  await player2.click(buttonWithText(p2Words[0]));
  await player2.click(buttonWithText(p2Words[1]));
  await screenshot(player2, 'player2', '05-1-selecting-words.png');

  await player2.click(buttonWithText("Start pitch"));
  await player2.waitForSelector(lifecycleHook("pitching"));
  await screenshot(player2, 'player2', '05-2-pitching.png');

  await player2.click(buttonWithText("Finish pitch"));
  await player2.waitForSelector(lifecycleHook("spectating"));
  await screenshot(player2, 'player2', '05-3-finished-pitching.png');

  await player1.waitForSelector('text="' + p2Words[0].toLowerCase() + ' ' + p2Words[1].toLowerCase() + '"');
  await screenshot(player1, 'player1', '05-3-buying.png');

  // player 1 selects player 2 as winner
  console.log("selecting winner");
  await player1.click(buttonWithText(p2Words[0] + ' ' + p2Words[1]));
  await player1.waitForSelector(lifecycleHook("spectating"));
  await screenshot(player1, 'player1', '05-4-finished-buying.png');

  // check scores
  console.log("checking scores");
  await screenshot(host, 'host', '05-3.5-point-awarded.png');
  await host.waitForSelector('xpath=//*[contains(@class, "point") and contains(text(), "1")]');
  await screenshot(host, 'host', '05-4-point-awarded.png');
  await player2.waitForSelector('xpath=//*[contains(@class, "point") and contains(text(), "1")]');
  await screenshot(player2, 'player2', '05-4-point-awarded.png');

  console.log("done");
}

// Utilities


async function screenshot(player, playerName, fileName) {
  // delay to allow transitions to complete
  await sleep(501);
  await player.screenshot({path: screenshotPath(playerName, fileName)});
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

function screenshotPath(player, filename) {
  return 'screenshots/' + player + '/' + filename;
}
