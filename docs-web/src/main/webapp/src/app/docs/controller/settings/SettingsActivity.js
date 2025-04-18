'use strict';

/**
 * Settings activity controller.
 */
angular.module('docs').controller('SettingsActivity', function($scope, $state, Restangular, $translate) {
  // Initialize variables
  $scope.loadingActivities = false;
  $scope.activities = [];
  $scope.total = 0;
  $scope.offset = 0;
  $scope.limit = 10;

  // Chart data for Gantt chart
  $scope.chartData = {
    labels: [],
    datasets: [
      {
        label: 'Completed',
        backgroundColor: '#2aabd2',
        data: []
      },
      {
        label: 'In Progress',
        backgroundColor: '#f0ad4e',
        data: []
      },
      {
        label: 'Not Started',
        backgroundColor: '#d9534f',
        data: []
      }
    ]
  };

  // Chart options
  $scope.chartOptions = {
    responsive: true,
    scales: {
      x: {
        stacked: true,
        title: {
          display: true,
          text: 'Users'
        }
      },
      y: {
        stacked: true,
        min: 0,
        max: 100,
        title: {
          display: true,
          text: 'Progress (%)'
        }
      }
    }
  };

  /**
   * Load user activities.
   */
  $scope.loadActivities = function() {
    $scope.loadingActivities = true;
    Restangular.one('useractivity')
      .get({
        offset: $scope.offset,
        limit: $scope.limit,
        sort_column: 0,
        asc: false
      })
      .then(function(data) {
        $scope.activities = data.activities;
        $scope.total = data.total;
        $scope.loadingActivities = false;
        
        // Process data for charts
        processChartData();
      });
  };

  /**
   * Process activity data for the Gantt chart.
   */
  function processChartData() {
    // Clear existing data
    $scope.chartData.labels = [];
    $scope.chartData.datasets[0].data = []; // Completed
    $scope.chartData.datasets[1].data = []; // In Progress
    $scope.chartData.datasets[2].data = []; // Not Started
    
    // Group activities by user
    var userActivities = {};
    $scope.activities.forEach(function(activity) {
      if (!userActivities[activity.username]) {
        userActivities[activity.username] = {
          total: 0,
          completed: 0,
          inProgress: 0,
          notStarted: 0,
          activities: []
        };
      }
      
      userActivities[activity.username].activities.push(activity);
      userActivities[activity.username].total++;
      
      if (activity.progress === 100) {
        userActivities[activity.username].completed++;
      } else if (activity.progress > 0) {
        userActivities[activity.username].inProgress++;
      } else {
        userActivities[activity.username].notStarted++;
      }
    });
    
    // Prepare chart data
    Object.keys(userActivities).forEach(function(username) {
      var userData = userActivities[username];
      $scope.chartData.labels.push(username);
      
      // Calculate percentages
      var completedPercent = userData.completed / userData.total * 100;
      var inProgressPercent = userData.inProgress / userData.total * 100;
      var notStartedPercent = userData.notStarted / userData.total * 100;
      
      $scope.chartData.datasets[0].data.push(completedPercent);
      $scope.chartData.datasets[1].data.push(inProgressPercent);
      $scope.chartData.datasets[2].data.push(notStartedPercent);
    });
  }

  /**
   * Load more activities.
   */
  $scope.loadMore = function() {
    $scope.offset += $scope.limit;
    $scope.loadActivities();
  };

  /**
   * Format date for display.
   */
  $scope.formatDate = function(timestamp) {
    if (!timestamp) return '';
    return new Date(timestamp).toLocaleDateString();
  };

  /**
   * Format progress for display.
   */
  $scope.formatProgress = function(progress) {
    if (progress === 100) {
      return $translate.instant('settings.user_activities.status.completed');
    } else if (progress > 0) {
      return $translate.instant('settings.user_activities.status.in_progress') + ' (' + progress + '%)';
    } else {
      return $translate.instant('settings.user_activities.status.not_started');
    }
  };

  /**
   * Get progress class.
   */
  $scope.getProgressClass = function(progress) {
    if (progress === 100) {
      return 'progress-bar-success';
    } else if (progress > 0) {
      return 'progress-bar-warning';
    } else {
      return 'progress-bar-danger';
    }
  };

  /**
   * Delete a user activity.
   */
  $scope.deleteActivity = function(activity) {
    if (confirm($translate.instant('settings.user_activities.confirm_delete'))) {
      Restangular.one('useractivity', activity.id).remove().then(function() {
        // Remove from the list
        $scope.activities = $scope.activities.filter(function(a) {
          return a.id !== activity.id;
        });
        $scope.total--;
        
        // Refresh chart data
        processChartData();
      });
    }
  };

  // Load activities on controller initialization
  $scope.loadActivities();
}); 