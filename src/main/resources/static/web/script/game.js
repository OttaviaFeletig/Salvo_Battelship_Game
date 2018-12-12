var dataObject = new Vue({
    
    el: '#shipLocationTable',
    data: {
        urlGameView: 'http://localhost:8080/api/game_view/',
        gamePlayerId: null,
        data: {},
        ships: [],
        gamePlayers: [],
        principalGamePlayer: "",
        opponentGamePlayer: "",
        gridNumbers: ["", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        gridLetters: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        cellsLocation: [],
        isLoading: true
    },
    created(){
        this.changeDinamicallyUrl();
        this.loadFetchGameView(this.urlGameView + this.gamePlayerId)
    },
    methods: {
        loadFetchGameView(url){
            fetch(url, {
                method: 'GET'
            })
                .then(response => response.json())
                .then(data => {
                this.isLoading = false;
                this.data = data;
                this.ships = data.ships;
                this.gamePlayers = data.gamePlayers
                console.log(this.data);
                console.log(this.gamePlayers);
                this.createGridCellsLocation();
                this.renderGamePlayers();
                this.convertDate();
            })
        },
        changeDinamicallyUrl(){
            let urlId = window.location.href.split("=");
            this.gamePlayerId = urlId[1];
        },
        createGridCellsLocation(){
            var allCells = [];
            for(var i = 0; i < this.gridLetters.length; i++){
                for(var j = 0; j < this.gridNumbers.length; j++){
                    allCells.push(this.gridLetters[i] + this.gridNumbers[j])
                }
                
            }
            for(var k = 0; k < allCells.length / 11; k++){
                var startSlice = k * 11;
                this.cellsLocation.push(allCells.slice(startSlice, startSlice + 11))
            }
        },
        renderShips(location){
            for(var i = 0; i < this.ships.length; i++){
                if(this.ships[i].locations.includes(location)){
                    return true;
                }   
            }
        },
        renderGamePlayers(){
            console.log(this.gamePlayers);
            for(var i = 0; i < this.gamePlayers.length; i++){
                if(this.gamePlayers[i].id == this.gamePlayerId){
                    this.principalGamePlayer = this.gamePlayers[i].player.email
                } else{
                    this.opponentGamePlayer = this.gamePlayers[i].player.email
                }
            }
            console.log(this.principalGamePlayer)
            console.log(this.opponentGamePlayer)
        },
        convertDate(){ 
            console.log(this.data.created)
            this.data.created = new Date(this.data.created).toLocaleString()
            console.log(this.data.created)
        }
        
    }















})