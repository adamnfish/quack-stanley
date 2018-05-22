import './main.css';
import { Main } from './Main.elm';

var app = Main.embed(document.getElementById('root'));


// saved games, should be in own module!
var savedGamesKey = "QS_SAVED_GAMES";

// TODO: clear out expired games here

app.ports.fetchSavedGames.subscribe(function() {
    console.log("sending game to Elm");
    var existingGames = JSON.parse(localStorage.getItem(savedGamesKey)) || [];
    console.log("games", existingGames);
    app.ports.savedGames.send(existingGames);
});

app.ports.sendGameToJS.subscribe(function(game) {
    // TODO only save if it is a new game ID/player key (prevent duplicates)
    console.log("game sent to JS", game);
    var games = JSON.parse(localStorage.getItem(savedGamesKey)) || [];
    console.log("games:", games);
    games.unshift(game);
    console.log("games after update", games);
    localStorage.setItem(savedGamesKey, JSON.stringify(games));
});
