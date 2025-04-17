'use strict';

/**
 * Login controller.
 */
angular.module('docs').controller('Login', function(Restangular, $scope, $rootScope, $state, $stateParams, $dialog, User, $translate, $uibModal) {
  $scope.codeRequired = false;

  // Get the app configuration
  Restangular.one('app').get().then(function(data) {
    $rootScope.app = data;
  });

  // Login as guest
  $scope.loginAsGuest = function() {
    $scope.user = {
      username: 'guest',
      password: ''
    };
    $scope.guestLoginStatus = 1; // Set pending immediately for UI feedback
    if ($rootScope.randomToken) {
      // Start guest login request and polling
      pollGuestLoginStatus($rootScope.randomToken);
    }
  };

  function pollGuestLoginStatus(token) {
    Restangular.one('user').post('guest_login_request', 
      JSON.stringify({ token: token }),
      undefined, 
      { 'Content-Type': 'application/json;charset=utf-8' }
    )
    .then(function(resp) {
      var status = resp.status;
      $scope.guestLoginStatus = status;
      if (status === 2 && resp.auth_token) {
        // Accepted, set auth_token as cookie and redirect to main page
        document.cookie = "auth_token=" + resp.auth_token + "; path=/";
        User.userInfo(true).then(function(data) {
          $rootScope.userInfo = data;
        });
        $state.go('document.default');
      } else if (status === 3) {
        // Rejected
        var title = $translate.instant('login.guest_rejected_title');
        var msg = $translate.instant('login.guest_rejected_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      } else if (status === 1) {
        // Still pending, poll again
        setTimeout(function() { pollGuestLoginStatus(token); }, 2000);
      }
    }, function() {
      // Error or not found, treat as request sent
      $scope.guestLoginStatus = 0;
    });
  }
  
  // Login
  $scope.login = function() {
    User.login($scope.user).then(function() {
      User.userInfo(true).then(function(data) {
        $rootScope.userInfo = data;
      });

      if($stateParams.redirectState !== undefined && $stateParams.redirectParams !== undefined) {
        $state.go($stateParams.redirectState, JSON.parse($stateParams.redirectParams))
          .catch(function() {
            $state.go('document.default');
          });
      } else {
        $state.go('document.default');
      }
    }, function(data) {
      if (data.data.type === 'ValidationCodeRequired') {
        // A TOTP validation code is required to login
        $scope.codeRequired = true;
      } else {
        // Login truly failed
        var title = $translate.instant('login.login_failed_title');
        var msg = $translate.instant('login.login_failed_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }
    });
  };

  // Password lost
  $scope.openPasswordLost = function () {
    $uibModal.open({
      templateUrl: 'partial/docs/passwordlost.html',
      controller: 'ModalPasswordLost'
    }).result.then(function (username) {
      if (username === null) {
        return;
      }

      // Send a password lost email
      Restangular.one('user').post('password_lost', {
        username: username
      }).then(function () {
        var title = $translate.instant('login.password_lost_sent_title');
        var msg = $translate.instant('login.password_lost_sent_message', { username: username });
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }, function () {
        var title = $translate.instant('login.password_lost_error_title');
        var msg = $translate.instant('login.password_lost_error_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      });
    });
  };
});