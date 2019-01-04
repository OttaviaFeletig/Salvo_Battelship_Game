var dataObj = new Vue({

    el: '#games',
    data: {
        urlArray: ['http://localhost:8080/api/games', 'http://localhost:8080/api/leader_board'],
        gameList: [],
        date: [],
        playersList: [],
        isLoading: true
    },
    created() {
        this.loadFetchGame(this.urlArray)
    },
    methods: {
        loadFetchGame(urlArray) {
            Promise.all(urlArray
                    .map(url => fetch(url)
                        .then(response => response.json())))
                .then(values => {
                    this.gameList = values[0];
                    this.playersList = values[1];

                    console.log(this.gameList)
                    console.log(this.playersList)
                    this.convertDate();
                    this.calculateTotalScore();
                    this.calculateResults();
                    this.isLoading = false;
                })
        },
        convertDate() {
            this.gameList.map(game => {
                game.created = new Date(game.created).toLocaleString()
                if(game.finished){
                    game.finished = new Date(game.finished).toLocaleString()
                }else{
                    game.finished = "still playing"
                }
                
            })
        },
        calculateTotalScore() {
            for (var i = 0; i < this.playersList.length; i++) {
                this.playersList[i]["total_score"] =
                    this.playersList[i].scores
                    .reduce((a, b) => a + b, 0) || "N/C";
            }
        },
        calculateResults() {
            for (var i = 0; i < this.playersList.length; i++) {
                let won = 0;
                let tied = 0;
                let lost = 0;
                if (this.playersList[i].scores.length > 0) {
                    this.playersList[i].scores.map(score => {
                        if (score < 0.5) {
                            lost++
                        } else if (score == 0.5) {
                            tied++
                        } else {
                            won++
                        }
                        this.playersList[i]["won"] = won;
                        this.playersList[i]["tied"] = tied;
                        this.playersList[i]["lost"] = lost;
                    })
                } else {
                    this.playersList[i]["won"] = "-";
                    this.playersList[i]["tied"] = "-";
                    this.playersList[i]["lost"] = "-";
                }
            }
        }
    }


})
