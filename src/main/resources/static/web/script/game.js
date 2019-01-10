var dataObject = new Vue({

    el: '#gameView',
    data: {
        urlGameView: 'http://localhost:8080/api/game_view/',
        gamePlayerId: null,
        data: {},
        ships: [],
        shipLocations: [],
        gamePlayers: [],
        salvos: [],
        salvoPrincipalLocations: [],
        salvoOpponentLocations: [],
        principalGamePlayer: "",
        opponentGamePlayer: "",
        gridNumbers: ["", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        gridLetters: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        cellsLocation: [],
        isLoading: true,
//        isLoggedOut: true
    },
    created() {
        
        this.createGridCellsLocation();
        this.changeDinamicallyUrl();
        this.loadFetchGameView(this.urlGameView + this.gamePlayerId)
        
    },
    methods: {
        loadFetchGameView(url) {
            console.log(url)
            fetch(url, {
                    method: 'GET'
                })
                .then(response => {
                if(response.status == 200){
                    return response.json()
                }else{
                    alert("You are not authorized, I will not let you cheat!")
                }
            }
            ).then(data => {
                    this.isLoading = false;
                    this.data = data;
                    this.ships = data.ships;
                    this.gamePlayers = data.gamePlayers;
                    this.salvos = data.salvos;
                    console.log(this.data);
                    console.log(this.salvos)
                    this.renderGamePlayers();
                    this.convertDate();
                    this.renderShips(this.ships);
                    this.renderSalvosPrincipal(this.salvos);
                    this.renderSalvosOpponent(this.salvos);
                })
        },
        changeDinamicallyUrl() {
            let urlId = window.location.href.split("=");
            this.gamePlayerId = urlId[1];
        },
        createGridCellsLocation() {
            var allCells = [];
            for (var i = 0; i < this.gridLetters.length; i++) {
                for (var j = 0; j < this.gridNumbers.length; j++) {
                    allCells.push(this.gridLetters[i] + this.gridNumbers[j])
                }

            }
            for (var k = 0; k < allCells.length / 11; k++) {
                var startSlice = k * 11;
                this.cellsLocation.push(allCells.slice(startSlice, startSlice + 11))
            }
        },
        renderShips(ships) {
            ships.forEach(ship => ship.location.forEach(location => this.shipLocations.push(location)))
            console.log(this.shipLocations)
            this.shipLocations.forEach(loc => document.querySelector(`#g1${loc}`).classList.add("ship"))
        },
        renderGamePlayers() {
            for (var i = 0; i < this.gamePlayers.length; i++) {
                if (this.gamePlayers[i].id == this.gamePlayerId) {
                    this.principalGamePlayer = this.gamePlayers[i].player.name
                } else {
                    this.opponentGamePlayer = this.gamePlayers[i].player.name
                }
            }
        },
        convertDate() {
            this.data.created = new Date(this.data.created).toLocaleString()
        },
        renderSalvosPrincipal(salvos) {
            for (var i = 0; i < salvos.length; i++) {
                if (salvos[i].gamePlayerId == this.gamePlayerId) {
                    console.log(salvos[i])
                    salvos[i].location.forEach(location => {
                        document.querySelector(`#g2${location}`).innerHTML = `<div class='salvo'>${salvos[i].turnNumber}</div>`;
//                        document.querySelector(`#g2${location}`).innerHTML = salvos[i].turnNumber;
                    })
                }
            }
        },
        renderSalvosOpponent(salvos) {
            for (var i = 0; i < salvos.length; i++) {
                if (salvos[i].gamePlayerId != this.gamePlayerId) {
                    salvos[i].location.forEach(location => {
                        this.shipLocations.forEach(shipLoc => {
                            if(location == shipLoc){
                                document.querySelector(`#g1${shipLoc}`).innerHTML = `<div class='hit'>${salvos[i].turnNumber}</div>`;
                            }else{
                               if(!document.querySelector(`#g1${location}`).childNodes[0]){
                                   document.querySelector(`#g1${location}`).innerHTML = `<div class='salvo'>${salvos[i].turnNumber}</div>`;
                               }
                            }
                        })
                    })
                }
            }
        },
        logOut(){
            fetch("/api/logout", {
                method: 'POST'
            })
            .then(response => {
                if(response.status == 200){
                    window.location.reload()
//                    this.isLoggedOut = true;
                }else{
                    alert("You didn't logout")
                }
            })
        }
    }









})
