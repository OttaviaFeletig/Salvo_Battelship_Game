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
        isLoading: true
    },
    created() {
        this.createGridCellsLocation();
        this.changeDinamicallyUrl();
        this.loadFetchGameView(this.urlGameView + this.gamePlayerId)
    },
    methods: {
        loadFetchGameView(url) {
            fetch(url, {
                    method: 'GET'
                })
                .then(response => response.json())
                .then(data => {
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
                    this.isLoading = false;

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
                    this.principalGamePlayer = this.gamePlayers[i].player.email
                } else {
                    this.opponentGamePlayer = this.gamePlayers[i].player.email
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
                        document.querySelector(`#g2${location}`).classList.add("salvo")
                        document.querySelector(`#g2${location}`).innerHTML = salvos[i].turnNumber;
                    })
                    //                    console.log(this.salvoPrincipalLocations)
                    //                    this.salvoPrincipalLocations.forEach(loc => {
                    //                        document.querySelector(`#g2${loc}`).classList.add("salvo")
                    //                        document.querySelector(`#g2${loc}`).innerHTML = salvos[i].turnNumber;
                    //                    })

                }
            }
        },
        renderSalvosOpponent(salvos) {
            for (var i = 0; i < salvos.length; i++) {
                if (salvos[i].gamePlayerId != this.gamePlayerId) {
                    salvos[i].location.forEach(location => {
                        this.shipLocations.forEach(shipLoc => {
                            if (location == shipLoc) {
                                document.querySelector(`#g1${shipLoc}`).classList.add("hit");
                                document.querySelector(`#g1${location}`).innerHTML = salvos[i].turnNumber;
                            }else {
                                document.querySelector(`#g1${location}`).classList.add("salvo");
                                document.querySelector(`#g1${location}`).innerHTML = salvos[i].turnNumber;
                            }
                        })
                    })
                }
            }
//            this.shipLocations.forEach(shipLoc => {
//                if (this.salvoOpponentLocations.includes(shipLoc)) {
//                    document.querySelector(`#g1${shipLoc}`).classList.add("hit")
//                } else {
//                    this.salvoOpponentLocations.forEach(salvoLoc => {
//                        document.querySelector(`#g1${salvoLoc}`).classList.add("salvo")
//                    })
//                }
//            })
        },



    }









})
