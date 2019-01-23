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
        salvoLocations: [],
        //        salvoPrincipalLocations: [],
        //        salvoOpponentLocations: [],
        principalGamePlayer: "",
        opponentGamePlayer: "",
        gridNumbers: ["", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        gridLetters: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        cellsLocation: [],
        isLoading: true,
        orientationOption: false,
        selectedShip: null,
        selectedShipForHtml: null,
        selectedOrientation: null,
        placingShipLocation: [],
        shipLength: null,
        errorLocation: [],
        oneShip: {},
        aircraftCarrier: {
            "shipType": "",
            "shipLocations": []
        },
        battleship: {
            "shipType": "",
            "shipLocations": []
        },
        submarine: {
            "shipType": "",
            "shipLocations": []
        },
        destroyer: {
            "shipType": "",
            "shipLocations": []
        },
        pBoat: {
            "shipType": "",
            "shipLocations": []
        },
        //        test: false,
        allShipsLocation: [],
        allShipType: [],
        temporaryLocation: [],
        messageToRemoveShip: false,
        shipAlreadyPlaced: false,
        sendingSalvos: {},
        turnNumber: 1,
        placingSalvoLocation: null,
        allSalvosLocation: []
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
                    console.log(this.salvos)
                    this.renderGamePlayers();
                    this.convertDate();
                    this.renderShips(this.ships);
                    this.renderSalvosPrincipal(this.salvos);
                    this.renderSalvosOpponent(this.salvos);
                    if (this.ships.length > 0) {
                        this.shipAlreadyPlaced = true
                    }
                    this.turnNumber = this.salvos.length + 1

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
            if (this.ships.length == 5) {
                fetch("/api/games/players/" + this.gamePlayerId + "/ships", {
                        method: 'POST',
                        credentials: 'include',
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(this.ships)
                    })
                    .then(response => {
                        console.log(response)
                        return response.json()
                    })
                    .then(data => {
                        console.log(data)
                        window.location.reload()
                    })
            } else {
                alert("You still have to place some ships!")
            }

        },
        sendSalvos() {
            if(this.sendingSalvos.salvoLocations.length == 5){
                fetch("/api/games/players/" + this.gamePlayerId + "/salvos", {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(this.sendingSalvos)
                })
                .then(response => {
                    console.log(response)
                    return response.json()
                })
                .then(data => {
                    console.log(data)
                    window.location.reload()
                    //                    this.sendingSalvos = {}
                    console.log(this.turnNumber)
                    console.log(this.sendingSalvos)
                })
            }else{
                alert("You still have to fire some salvos!")
            }
            
        },
        handler(event) {
            if (this.checkIfShipAlreadyExists(event.toElement.value) == false) {
                this.getShipValue(event)
                this.showOrientation()
                this.convertSelectedShipForHtml()
            } else {
                this.removeShip(event.toElement.value)
                this.unfadeButton(event.target.id)
            }

        },
        fadeButton(buttonId) {
            //            console.log(event)
            document.querySelector(`#${buttonId}`).classList.add("fade_button")
            //            document.querySelector(`#${event.target.id}`).classList.add("fade_button")
        },
        unfadeButton(buttonId) {
            document.querySelector(`#${buttonId}`).classList.remove("fade_button")
        },
        checkIfShipAlreadyExists(buttonValue) {
            if (this.ships.length != 0) {
                this.allShipType = this.ships.map(oneShip => oneShip.shipType)
                if (this.allShipType.includes(buttonValue)) {
                    return true
                } else {
                    return false
                }
            } else {
                return false
            }
        },
        getShipValue(event) {
            console.log(event.toElement.value)
            this.selectedShip = event.toElement.value
            this.selectedOrientation = 'horizontal'

        },
        removeShip(buttonValue) {
            this.ships.forEach(ship => {
                if (buttonValue == ship.shipType) {
                    this.temporaryLocation = ship.shipLocations
                    //                    console.log(this.temporaryLocation)
                    ship.shipLocations = []
                    this.selectedShip = ship.shipType
                }
            })
            for (var i = 0; i < this.temporaryLocation.length; i++) {
                document.querySelector(`#g1${this.temporaryLocation[i]}`).classList.remove("ship")
            }
            this.showOrientation()
            //            this.recognizeShip(this.temporaryLocation[1])
            console.log(this.ships)
            //            console.log(this.selectedShip)
        },
        showOrientation() {
            this.orientationOption = true;
        },
        recognizeShip(location) {
            //            console.log(this.selectedShip)
            //            console.log(location)
            //            let oneShip = {}
            if (this.selectedShip == "aircraft_carrier") {
                this.shipLength = 5;
                this.oneShip = this.aircraftCarrier
            }
            if (this.selectedShip == "battleship") {
                this.shipLength = 4
                this.oneShip = this.battleship
            }
            if (this.selectedShip == "submarine") {
                this.shipLength = 3
                this.oneShip = this.submarine
            }
            if (this.selectedShip == "destroyer") {
                this.shipLength = 3
                this.oneShip = this.destroyer
            }
            if (this.selectedShip == "p_boat") {
                this.shipLength = 2
                this.oneShip = this.pBoat
            }
            //            console.log(this.oneShip)
            this.hoverShipOnGrid(location, this.shipLength)


        },
        hoverShipOnGrid(location, shipLength) {
            if (this.selectedShip != null && this.selectedOrientation != null) {
                var locationNumber = []
                var asciiLocation = []
                var locationLetter = []
                console.log(location)
                for (var i = 0; i < shipLength; i++) {
                    locationNumber.push(parseInt(location.substr(1, 2)) + i)
                    asciiLocation.push(location.charCodeAt(location.substr(0, 1)) + i)
                    locationLetter.push(String.fromCharCode(asciiLocation[i]))
                }
                if (this.selectedOrientation == "horizontal") {
                    if (locationNumber[locationNumber.length - 1] < 11) {
                        locationNumber
                            .forEach(oneNumber => this.placingShipLocation.push(location.split("")[0] + oneNumber))
                        if (this.checkIfShipLocationIsEqual() == false) {
                            this.showHoverBlueColor()
                        } else {
                            this.showOverlappingHoverRedColor()

                            //                            document.querySelector(`#g1${location}`).removeEventListener("@click", this.placeShipOnGrid())
                        }
                    } else {
                        var numberOutGrid = locationNumber.filter(oneLocation => oneLocation < 11)
                        numberOutGrid.forEach(oneNumber => this.errorLocation.push(location.split("")[0] + oneNumber))
                        this.errorLocation
                            .map(oneCell => {
                                document.querySelector(`#g1${oneCell}`).classList.add("error_hover")
                            })
                        //                        document.querySelector(`#g1${location}`).removeEventListener("@click", this.placeShipOnGrid())
                    }
                }
                if (this.selectedOrientation == "vertical") {
                    if (asciiLocation[asciiLocation.length - 1] < 75) {
                        locationLetter
                            .forEach(oneLetter => this.placingShipLocation.push(oneLetter + location.substr(1, 2)))
                        if (this.checkIfShipLocationIsEqual() == false) {
                            this.showHoverBlueColor()
                        } else {
                            this.showOverlappingHoverRedColor()
                            //                            document.querySelector(`#g1${location}`).removeEventListener("@click", this.placeShipOnGrid())
                        }
                    } else {
                        var asciiOutGrid = asciiLocation.filter(oneLocation => oneLocation < 75)
                        var letterOutGrid = []
                        for (var i = 0; i < asciiOutGrid.length; i++) {
                            letterOutGrid.push(String.fromCharCode(asciiOutGrid[i]))
                        }
                        //                        console.log(letterOutGrid)
                        letterOutGrid.forEach(oneLetter => this.errorLocation.push(oneLetter + location.substr(1, 2)))
                        this.errorLocation
                            .map(oneCell => {
                                document.querySelector(`#g1${oneCell}`).classList.add("error_hover")
                            })
                        //                        document.querySelector(`#g1${location}`).removeEventListener("@click", this.placeShipOnGrid())
                    }
                }
                console.log(this.placingShipLocation)
            }

        },
        showHoverBlueColor() {
            this.placingShipLocation
                .map(oneCell => document.querySelector(`#g1${oneCell}`).classList.add("ship_hover"))
            //            this.oneShip.type = this.selectedShip
            //            this.oneShip.location = this.placingShipLocation
        },
        showOverlappingHoverRedColor() {
            this.placingShipLocation
                .map(oneCell => document.querySelector(`#g1${oneCell}`).classList.add("error_hover"))
        },
        removeShipHover(location) {
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
        placeShipOnGrid(location) {
            if (this.selectedShip != null && this.selectedOrientation != null) {
                if (this.checkIfShipAlreadyExists(this.selectedShip) == false) {

                    if (this.placingShipLocation.length == this.shipLength) {

                        if (this.checkIfShipLocationIsEqual() == false) {
                            if (this.selectedShip == "aircraft_carrier") {
                                this.oneShip.shipType = this.selectedShip
                                this.oneShip.shipLocations = this.placingShipLocation
                                this.ships.push(this.aircraftCarrier)
                                this.fadeButton("aircraft_carrier")
                            }
                            if (this.selectedShip == "battleship") {
                                this.oneShip.shipType = this.selectedShip
                                this.oneShip.shipLocations = this.placingShipLocation
                                this.ships.push(this.battleship)
                                this.fadeButton("battleship")
                            }
                            if (this.selectedShip == "submarine") {
                                this.oneShip.shipType = this.selectedShip
                                this.oneShip.shipLocations = this.placingShipLocation
                                this.ships.push(this.submarine)
                                this.fadeButton("submarine")
                            }
                            if (this.selectedShip == "destroyer") {
                                this.oneShip.shipType = this.selectedShip
                                this.oneShip.shipLocations = this.placingShipLocation
                                this.ships.push(this.destroyer)
                                this.fadeButton("destroyer")
                            }
                            if (this.selectedShip == "p_boat") {
                                this.oneShip.shipType = this.selectedShip
                                this.oneShip.shipLocations = this.placingShipLocation
                                this.ships.push(this.pBoat)
                                this.fadeButton("p_boat")
                            }
                            console.log(this.ships)
                            this.placingShipLocation.forEach(loc => {
                                document.querySelector(`#g1${loc}`).classList.remove("ship_hover")
                                document.querySelector(`#g1${loc}`).classList.add("ship")
                            })
                            this.selectedShip = null
                            this.selectedOrientation = "horizontal"
                            this.orientationOption = false
                            this.placingShipLocation = []
                            this.allShipsLocation = []
                            this.messageToRemoveShip = true
                        }
                    }
                } else {
                    for (var i = 0; i < this.ships.length; i++) {
                        if (this.ships[i].shipType == this.selectedShip) {
                            this.ships[i].shipLocations = this.placingShipLocation;

                            break;
                        }
                    }
                    this.placingShipLocation.forEach(loc => {
                        document.querySelector(`#g1${loc}`).classList.remove("ship_hover")
                        document.querySelector(`#g1${loc}`).classList.add("ship")
                    })
                    this.fadeButton(this.selectedShip)
                    this.selectedShip = null
                    this.selectedOrientation = "horizontal"
                    this.orientationOption = false
                    this.placingShipLocation = []
                    this.allShipsLocation = []

                }
            }

        },
        checkIfShipLocationIsEqual() {
            if (this.ships.length == 0 || (this.ships.length == 1 && this.ships[0].shipLocations.length == 0)) {
                console.log("false")
                return false
            } else {

                this.allShipsLocation = [].concat.apply([], this.ships.map(oneShip => oneShip.shipLocations))
                //                console.log(this.allShipsLocation)
                for (var i = 0; i < this.allShipsLocation.length; i++) {
                    if (this.placingShipLocation.includes(this.allShipsLocation[i])) {
                        console.log("no")
                        return true
                    }
                }
                console.log("yes")
                return false
            }

        },
        convertSelectedShipForHtml() {
            this.selectedShipForHtml = this.selectedShip.split("_").map(word => word.charAt(0).toUpperCase() + word.substring(1)).join(" ")
        },
        hoverSalvoOnGrid(location) {
            this.placingSalvoLocation = location
            if (this.checkIfSalvoLocationIsEqual() == false) {
                document.querySelector(`#g2${this.placingSalvoLocation}`).classList.add("salvo_hover")
            } else {
                document.querySelector(`#g2${this.placingSalvoLocation}`).classList.add("error_hover")
            }

        },
        removeSalvoHover(location) {
            document.querySelector(`#g2${this.placingSalvoLocation}`).classList.remove("salvo_hover")
            document.querySelector(`#g2${this.placingSalvoLocation}`).classList.remove("error_hover")
        },
        placeSalvoOnGrid(location) {
            if (this.checkIfSalvoLocationIsEqual() == false) {
                console.log(this.salvoLocations)
                if (this.salvoLocations.includes(location)) {
                    this.removeSalvoFromGrid(location)
                } else {
                    if(this.salvoLocations.length < 5){
                        document.querySelector(`#g2${location}`).classList.add("salvo")
                    this.salvoLocations.push(location)
                    }
                    
                }

            }


            console.log(this.turnNumber)
            this.sendingSalvos.turnNumber = this.turnNumber
            this.sendingSalvos.salvoLocations = this.salvoLocations

            console.log(this.sendingSalvos)

        },
        checkIfSalvoLocationIsEqual() {
            this.allSalvosLocation = [].concat.apply([], this.salvos.map(oneSalvo => oneSalvo.location))
            if (this.allSalvosLocation.length == 0) {
                return false
            } else {

                if (this.allSalvosLocation.includes(this.placingSalvoLocation)) {
                    return true
                }
                return false
            }

        },
        removeSalvoFromGrid(location) {

            document.querySelector(`#g2${location}`).classList.remove("salvo")
            this.salvoLocations.splice(this.salvoLocations.indexOf(location), 1)

        }
    }

})
