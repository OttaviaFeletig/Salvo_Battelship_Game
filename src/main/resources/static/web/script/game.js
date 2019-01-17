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
        testShipList: [{
                "shipType": "destroyer",
                "shipLocations": ["A1", "B1", "C1"]
            },
            {
                "shipType": "patrol boat",
                "shipLocations": ["H5", "H6"]
            }
        ],
        orientationOption: false,
        selectedShip: null,
        selectedOrientation: null,
        placingShipLocation: [],
        shipLength: null,
        errorLocation: [],
        aircraftCarrier: {
            "type": "",
            "location": []
        },
        battleship: {
            "type": "",
            "location": []
        },
        submarine: {
            "type": "",
            "location": []
        },
        destroyer: {
            "type": "",
            "location": []
        },
        pBoat: {
            "type": "",
            "location": []
        },
        test: false,
        allShipsLocation: [],
        shipList: []
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
                    if (response.status == 200) {
                        return response.json()
                    } else {
                        alert("You are not authorized, I will not let you cheat!")
                    }
                }).then(data => {
                    this.isLoading = false;
                    this.data = data;
                    this.ships = data.ships;
                    console.log(this.ships)
                    this.gamePlayers = data.gamePlayers;
                    this.salvos = data.salvos;
                    //                    console.log(this.data);
                    //                    console.log(this.salvos)
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
            //            console.log(this.shipLocations)
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
                            if (location == shipLoc) {
                                document.querySelector(`#g1${shipLoc}`).innerHTML = `<div class='hit'>${salvos[i].turnNumber}</div>`;
                            } else {
                                if (!document.querySelector(`#g1${location}`).childNodes[0]) {
                                    document.querySelector(`#g1${location}`).innerHTML = `<div class='salvo'>${salvos[i].turnNumber}</div>`;
                                }
                            }
                        })
                    })
                }
            }
        },
        logOut() {
            fetch("/api/logout", {
                    method: 'POST'
                })
                .then(response => {
                    if (response.status == 200) {
                        window.location.reload()
                        //                    this.isLoggedOut = true;
                    } else {
                        alert("You didn't logout")
                    }
                })
        },
        sendShips() {
            fetch("/api/games/players/" + this.gamePlayerId + "/ships", {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(this.testShipList)
                })
                .then(response => {
                    console.log(response)
                    return response.json()
                })
                .then(data => {
                    console.log(data)
                    window.location.reload()
                })
        },
        handler(event) {
            this.getShipValue(event)
            this.showOrientation()
        },
        getShipValue(event) {
            console.log(event.toElement.value)
            this.selectedShip = event.toElement.value
            this.selectedOrientation = null
        },
        showOrientation() {
            this.orientationOption = true;
        },
        recognizeShip(location) {
            let oneShip = {}
            if (this.selectedShip == "aircraft_carrier") {
                this.shipLength = 5;
                oneShip = this.aircraftCarrier
            }
            if (this.selectedShip == "battleship") {
                this.shipLength = 4
                oneShip = this.battleship
            }
            if (this.selectedShip == "submarine") {
                this.shipLength = 3
                oneShip = this.submarine
            }
            if (this.selectedShip == "destroyer") {
                this.shipLength = 3
                oneShip = this.destroyer
            }
            if (this.selectedShip == "p_boat") {
                this.shipLength = 2
                oneShip = this.pBoat
            }
            this.hoverShipOnGridHorizontal(location, this.shipLength, oneShip)


        },
        hoverShipOnGridHorizontal(location, shipLength, oneShip) {
            if (this.selectedShip != null && this.selectedOrientation == "horizontal") {
                var locationNumber = [];
                for (var i = 0; i < shipLength; i++) {
                    locationNumber.push(parseInt(location.substr(1, 2)) + i)
                }
                if (locationNumber[locationNumber.length - 1] < 11) {
                    locationNumber
                        .forEach(oneNumber => this.placingShipLocation.push(location.split("")[0] + oneNumber))
                    if (this.checkIfShipLocationIsEqual() == false) {
                        this.placingShipLocation
                            .map(oneCell => document.querySelector(`#g1${oneCell}`).classList.add("ship_hover"))
                        oneShip.type = this.selectedShip
                        oneShip.location = this.placingShipLocation
                    } else {
                        this.placingShipLocation
                            .map(oneCell => document.querySelector(`#g1${oneCell}`).classList.add("error_hover"))
                    }

                } else {
                    var numberOutGrid = locationNumber.filter(oneLocation => oneLocation < 11)
                    numberOutGrid.forEach(oneNumber => this.errorLocation.push(location.split("")[0] + oneNumber))
                    this.errorLocation
                        .map(oneCell => {
                            document.querySelector(`#g1${oneCell}`).classList.add("error_hover")
                        })
                }

            }
        },
        removeHover(location) {
            if (this.selectedShip != null && this.selectedOrientation != null) {
                this.placingShipLocation
                    .map(oneCell => {
                        document.querySelector(`#g1${oneCell}`).classList.remove("ship_hover")
                        document.querySelector(`#g1${oneCell}`).classList.remove("error_hover")
                    })
                this.placingShipLocation = []
                this.errorLocation
                    .map(oneCell => document.querySelector(`#g1${oneCell}`).classList.remove("error_hover"))
                this.errorLocation = []
            }
        },
        placeShipOnGrid() {
            if (this.selectedShip != null && this.selectedOrientation != null) {
                if (this.placingShipLocation.length == this.shipLength) {
                    if (this.checkIfShipLocationIsEqual() == false) {
                        if (this.selectedShip == "aircraft_carrier") {
                            this.ships.push(this.aircraftCarrier)
                        }
                        if (this.selectedShip == "battleship") {
                            this.ships.push(this.battleship)
                        }
                        if (this.selectedShip == "submarine") {
                            this.ships.push(this.submarine)
                        }
                        if (this.selectedShip == "destroyer") {
                            this.ships.push(this.destroyer)
                        }
                        if (this.selectedShip == "p_boat") {
                            this.ships.push(this.pBoat)
                        }
                        console.log(this.ships)
                        this.placingShipLocation.forEach(loc => {
                            document.querySelector(`#g1${loc}`).classList.remove("ship_hover")
                            document.querySelector(`#g1${loc}`).classList.add("ship")
                        })
                        this.selectedShip = null
                        this.placingShipLocation = []
                        this.allShipsLocation = []
                    } else {
                        alert("You can't place the ship there!")
                    }
                } else {
                    alert("You can't place the ship there!")
                }
            }

        },
        checkIfShipLocationIsEqual() {
            if (this.ships.length == 0) {
                //                console.log("false")
                return false
            } else {

                this.allShipsLocation = [].concat.apply([], this.ships.map(oneShip => oneShip.location))
                //                console.log(this.allShipsLocation)
                for (var i = 0; i < this.allShipsLocation.length; i++) {
                    if (this.placingShipLocation.includes(this.allShipsLocation[i])) {
                        //                        console.log("no")
                        return true
                    }
                }
                //                console.log("yes")
                return false
            }

        }
    }









})
