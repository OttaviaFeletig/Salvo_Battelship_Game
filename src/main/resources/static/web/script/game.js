var dataObject = new Vue({
    
    el: '#gameView',
    data: {
        urlGameView: 'http://localhost:8080/api/game_view/',
        gamePlayerId: null,
        data: {},
        ships: [],
        gamePlayers: [],
        salvos: [],
        principalGamePlayer: "",
        opponentGamePlayer: "",
        gridNumbers: ["", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        gridLetters: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        cellsLocation: [],
        isLoading: false
    },
    created(){
        this.createGridCellsLocation();
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
                this.data = data;
                this.ships = data.ships;
                this.gamePlayers = data.gamePlayers;
                this.salvos = data.salvos;
                console.log(this.data);
                console.log(this.salvos)
                this.renderGamePlayers();
                this.convertDate();
                this.isLoading = true;

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
                if(this.ships[i].location.includes(location)){
                    return true;
                }   
            }
        },
        renderGamePlayers(){
            for(var i = 0; i < this.gamePlayers.length; i++){
                if(this.gamePlayers[i].id == this.gamePlayerId){
                    this.principalGamePlayer = this.gamePlayers[i].player.email
                } else{
                    this.opponentGamePlayer = this.gamePlayers[i].player.email
                }
            }
        },
        convertDate(){
            this.data.created = new Date(this.data.created).toLocaleString()
        },
        renderSalvosPrincipal(location){
            for(var i = 0; i < this.salvos.length; i++){
                if(this.salvos[i].gamePlayerId == this.gamePlayerId){
                    if(this.salvos[i].location.includes(location)){
                        return true;
                    }
                }
            }
        },
        renderSalvosOpponent(location){
            for(var i = 0; i < this.salvos.length; i++){
                if(this.salvos[i].gamePlayerId != this.gamePlayerId){
                     if(this.salvos[i].location.includes(location)){
                        const div = this.$el
//                        console.log(div.childNodes[0])
                        console.log(div.querySelector(`#g1${location}`))
        console.log(document.querySelector(`#g1${location}`).classList.contains(".ship"))
//                        if(document.querySelector(`#g1${location}`).classList){
//                            console.log("colpito")
//                        }else{
//                          return true;  
//                        }
                        
                    }
                }
            }
        },
        

        
    }















})