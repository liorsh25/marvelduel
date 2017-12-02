'use strict';

var app = angular.module("gameApp", []);

app.controller('gameController', gameController);
app.controller('leadboardController', leadboardController);

//************************ Duel game controller **********************

function gameController($scope,$http,$timeout) {
    var getGameUrl = "/duel/getGame";
    var sendVoteUrl = "/duel/vote";
    $scope.duelsList = [];
    $scope.currentDuel = null;
    $scope.currDuelIndex = 0;
    $scope.succVote = true;
    $scope.showAfterSelection = false;

    loadNewGame();

    function loadNewGame(){
        $http.get(getGameUrl)
            .then(function(response) {
                $scope.duelsList = response.data.duels;
                console.log("$scope.duelsList="+$scope.duelsList);
                $scope.currentDuel = $scope.duelsList[$scope.currDuelIndex];
            },function(data) {
                console.error("error in request:"+getGameUrl)

            });
    }

   $scope.selectHero = function(heroObj){
        sendVote(heroObj);
        markAsSelected(heroObj);
   }


    function markAsSelected(heroObj){
        heroObj.selected = true;
        $scope.showAfterSelection = true;
        //Will show the V X marks for a sec and then continue to next duel
        $timeout(continueAfterSelection,1000);
   }

    function continueAfterSelection(){
     if($scope.currDuelIndex < $scope.duelsList.length-1){
            goToNextDuel($scope.currDuelIndex);
     }else{
          //go to leadboard
            window.location.href = "leadboard.html";
     }
    }

   function goToNextDuel(){
        $scope.currDuelIndex = $scope.currDuelIndex+1;
        console.log("Change currDuelIndex to "+ $scope.currDuelIndex);
        $scope.currentDuel = $scope.duelsList[$scope.currDuelIndex];
        $scope.showAfterSelection = false;
   }

   function sendVote(heroObj){

       var data = {
        duelId:$scope.currentDuel.duelId,
        heroId:heroObj.heroId
       };

       var config = {params: data};
        console.log("going to call vote with params:"+ data.duelId +","+ data.heroId);
        $http.get(sendVoteUrl,config)
               .then(function(response) {
                  $scope.succVote = response.data;
                  console.log("Got vote response="+$scope.succVote);
               },function(data) {
                   console.error("error in request:"+sendVoteUrl)

               });
       }

}

//************************ leadboard controller **********************

function leadboardController($scope,$http) {
    var getLeadBoardUrl = "/duel/getLeadBoard";
    $scope.heroesList = [];

    loadLeadBoard();

    function loadLeadBoard(){
        $http.get(getLeadBoardUrl)
           .then(function(response) {
               $scope.heroesList = response.data;
               console.log("$scope.heroesList="+$scope.heroesList);
           },function(data) {
               console.error("error in request:"+getLeadBoardUrl)
            });
    }
}