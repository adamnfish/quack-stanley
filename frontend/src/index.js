import './main.css';
import { Main } from './Main.elm';

var app = Main.embed(document.getElementById('root'));


// saved games, should be in own module!
var savedGamesKey = "QS_SAVED_GAMES";

// TODO: clear out expired games here

app.ports.fetchSavedGames.subscribe(function() {
    var existingGames = localStorage.getItem(savedGamesKey)
    app.ports.savedGames.send(existingGames);
});

app.ports.saveGame.subscribe(function(game) {
    var existingGames = localStorage.getItem(savedGamesKey);
    localStorage.setItem(savedGamesKey, existingGames.unshift(game));
});
