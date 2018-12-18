var dataObj = new Vue({
    
    el: '#leaderBoard',
    data: {
        urlGame: 'http://localhost:8080/api/leader_board',
        playersList: [],
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
                    this.playersList = data;
                    console.log(this.playersList);    
            })
        }
    }
    
    
})