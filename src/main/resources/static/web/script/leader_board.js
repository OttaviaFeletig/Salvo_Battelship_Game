var dataObj = new Vue({

    el: '#leaderBoard',
    data: {
        urlGame: 'http://localhost:8080/api/leader_board',
        playersList: [],
        isLoading: true
    },
    created() {
        this.loadFetchGame(this.urlGame)

    },
    methods: {
        loadFetchGame(url) {
            fetch(url, {
                    method: 'GET',
                })
                .then(response => response.json())
                .then(data => {
                    this.playersList = data;
                    console.log(this.playersList);
                    this.calculateTotalScore();
                    console.log(this.playersList);
                    this.calculateResults();
                    console.log(this.playersList);
                    this.isLoading = false;
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
                }else{
                    this.playersList[i]["won"] = "-";
                    this.playersList[i]["tied"] = "-";
                    this.playersList[i]["lost"] = "-";
                }
            }
        }
    },


})
