var dataObj = new Vue({
    
    el: '#gameList',
    data: {
        urlGame: 'http://localhost:8080/api/games',
        gameList: [],
        isLoading: true        
    },
    created(){
        this.loadFetchGame(this.urlGame)
    },
    methods: {
        loadFetchGame(url){
            fetch(url, {
                method: 'GET',
            })
                .then(response => response.json())
                .then(data => {
                this.isLoading = false;
                this.gameList = data;
                console.log(this.gameList)
            })
        }
    }
    
    
})