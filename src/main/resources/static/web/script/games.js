var dataObj = new Vue({

    el: '#games',
    data: {
        urlArray: ['http://localhost:8080/api/games', 'http://localhost:8080/api/leader_board'],
        gameList: [],
        date: [],
        playersList: [],
        emailLogIn: "",
        passwordLogIn: "",
        nameSignIn: "",
        emailSignIn: "",
        passwordSignIn: "",
        isLoading: true,
        isRegistered: true,
        playerLoggedIn: null,
        viewingPlayerId: ""
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
                    this.isLoading = false;
                    this.playerLoggedIn = values[0].player
                    console.log(this.playerLoggedIn)
                    this.gameList = values[0].games;
                    console.log(this.gameList)
                    this.playersList = values[1];
                    console.log(this.gameList)
                    console.log(this.playersList)
                    this.convertDate();
                    this.calculateTotalScore();
                    this.calculateResults();
                    //                    if(this.playerLoggedIn != null && this.gameList.length > 0){
                    //                        this.setGameAttribute();
                    //                    }
                })
        },
        convertDate() {
            this.gameList.map(game => {
                game.created = new Date(game.created).toLocaleString()
                if (game.finished) {
                    game.finished = new Date(game.finished).toLocaleString()
                } else {
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
        },
        logIn() {
            fetch("/api/login", {
                    method: 'POST',
                    credentials: "include",
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: `email=${ this.emailLogIn }&password=${ this.passwordLogIn }`
                })
                .then(response => {
                    if (response.status == 200) {
                        console.log("logged in!")

                    } else {
                        alert("Invalid email or password")
                    }
                })
                .catch(error => error)
        },
        logOut() {
            fetch("/api/logout", {
                    method: 'POST'
                })
                .then(response => {
                    if (response.status == 200) {
                        window.location.reload()
                        this.playerLoggedIn = null
                    } else {
                        alert("You didn't logout")
                    }
                })
        },
        signIn() {
            fetch("/api/players", {
                    method: 'POST',
                    credentials: "include",
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: `email=${ this.emailSignIn }&password=${ this.passwordSignIn }&name=${ this.nameSignIn }`
                })
                .then(response => {
                    if (response.status == 201) {
                        console.log("user added")
                        this.emailLogIn = this.emailSignIn
                        this.passwordLogIn = this.passwordSignIn
                        this.logIn()
                    } else if (response.status == 409) {
                        alert("This email has been already used")
                    } else {
                        alert("Field missing")
                    }

                })
        },
        register() {
            this.isRegistered = false
        },
        checkPlayer(game) {
//            console.log(game.gamePlayers.length)
            for(let i = 0; i < game.gamePlayers.length; i++){
                
                if(game.gamePlayers[i] && this.playerLoggedIn){
                    if(this.playerLoggedIn.id == game.gamePlayers[i].player.id){
                        console.log("yes")
                        this.viewingPlayerId = game.gamePlayers[i].id
//                        console.log(this.viewingPlayerId)
                        return true
                    }
                }else{
                    return false
                }
                
            }
        }
    }


})
