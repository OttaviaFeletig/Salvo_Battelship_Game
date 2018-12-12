var dataObject = new Vue({
    
    el: '#shipLocationTable',
    data: {
        urlGameView: 'http://localhost:8080/api/game_view/',
        gamePlayerId: null,
        data: {},
        ships: [],
        shipLocations: [],
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
                console.log(this.data);
                console.log(this.ships);
                this.createGridCellsLocation();                
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
            console.log(this.cellsLocation)
        },
        renderShips(location){
            console.log(location)
            for(var i = 0; i < this.ships.length; i++){
                
                console.log(this.ships[i].locations.includes(location))
//                return this.ships[i].locations.includes(location)
                if(this.ships[i].locations.includes(location)){
                    return true;
                }
                
            }
        },
//        locateShips(ships){
//            for(var i = 0; i < ships.length; i++){
//                
//            }
//        }
        
    }















})