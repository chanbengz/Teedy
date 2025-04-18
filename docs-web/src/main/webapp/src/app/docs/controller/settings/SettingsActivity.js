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
  $scope.limit = 50; // Increased limit for better Gantt visualization
  $scope.showGantt = true; // Toggle between Gantt and list view
  $scope.filterUser = null;
  $scope.filterType = null;
  $scope.availableUsers = [];
  $scope.availableTypes = [];

  // Gantt chart data
  $scope.ganttData = {
    data: [],
    timeScale: {
      from: new Date(),
      to: new Date(),
      width: '1000px'
    }
  };

  /**
   * Toggle between Gantt and list view
   */
  $scope.toggleView = function() {
    $scope.showGantt = !$scope.showGantt;
    if ($scope.showGantt) {
      prepareGanttData();
    }
  };

  /**
   * Load user activities.
   */
  $scope.loadActivities = function() {
    $scope.loadingActivities = true;
    
    // Build query parameters
    var params = {
      offset: $scope.offset,
      limit: $scope.limit,
      sort_column: 9, // Sort by create date by default
      asc: false
    };
    
    // Add filters if defined
    if ($scope.filterUser) {
      params.user_id = $scope.filterUser;
    }
    
    if ($scope.filterType) {
      params.activity_type = $scope.filterType;
    }
    
    Restangular.one('useractivity')
      .get(params)
      .then(function(data) {
        $scope.activities = data.activities;
        $scope.total = data.total;
        $scope.loadingActivities = false;
        
        // Extract unique users and activity types for filtering
        extractFilters();
        
        // Prepare Gantt data
        if ($scope.showGantt) {
          prepareGanttData();
        }
      });
  };

  /**
   * Extract unique users and activity types for filtering.
   */
  function extractFilters() {
    var users = {};
    var types = {};
    
    $scope.activities.forEach(function(activity) {
      users[activity.user_id] = activity.username;
      types[activity.activity_type] = activity.activity_type;
    });
    
    $scope.availableUsers = Object.keys(users).map(function(id) {
      return { id: id, name: users[id] };
    });
    
    $scope.availableTypes = Object.keys(types).map(function(type) {
      return { id: type, name: formatActivityType(type) };
    });
  }

  /**
   * Format activity type for display.
   */
  function formatActivityType(type) {
    // Convert snake_case to Title Case
    return type.split('_')
      .map(function(word) { return word.charAt(0).toUpperCase() + word.slice(1); })
      .join(' ');
  }

  /**
   * Prepare data for the Gantt chart.
   */
  function prepareGanttData() {
    var ganttRows = [];
    var minDate = new Date();
    var maxDate = new Date();
    
    // Track min/max dates for the time scale
    minDate.setDate(minDate.getDate() - 30); // Default to 30 days ago
    maxDate.setDate(maxDate.getDate() + 30); // Default to 30 days ahead
    
    // Group by user
    var userGroups = {};
    
    $scope.activities.forEach(function(activity) {
      if (!userGroups[activity.username]) {
        userGroups[activity.username] = [];
      }
      
      // Determine start and end dates for the activity
      var startDate = activity.create_timestamp ? new Date(activity.create_timestamp) : new Date();
      var endDate;
      
      if (activity.completed_date_timestamp) {
        endDate = new Date(activity.completed_date_timestamp);
      } else if (activity.planned_date_timestamp) {
        endDate = new Date(activity.planned_date_timestamp);
      } else {
        // If no dates specified, use a default duration
        endDate = new Date(startDate);
        endDate.setDate(endDate.getDate() + 7); // Default 7-day duration
      }
      
      // Update min/max dates for time scale
      if (startDate < minDate) minDate = startDate;
      if (endDate > maxDate) maxDate = endDate;
      
      // Add to user's activities
      userGroups[activity.username].push({
        id: activity.id,
        name: activity.entity_name || formatActivityType(activity.activity_type),
        start: startDate,
        end: endDate,
        progress: activity.progress,
        color: getTaskColor(activity.progress)
      });
    });
    
    // Convert user groups to Gantt rows with overlap detection
    Object.keys(userGroups).forEach(function(username) {
      // Sort tasks by start date
      var tasks = userGroups[username].sort(function(a, b) {
        return a.start - b.start;
      });
      
      // Detect and adjust for overlapping tasks
      var processedTasks = [];
      tasks.forEach(function(task) {
        // Default position is 0 (no vertical offset)
        task.verticalPosition = 0;
        
        // Find overlapping tasks that have already been processed
        var overlappingTasks = processedTasks.filter(function(existingTask) {
          return !(task.end <= existingTask.start || task.start >= existingTask.end);
        });
        
        // If there are overlapping tasks, find the first available vertical position
        if (overlappingTasks.length > 0) {
          // Get all used positions
          var usedPositions = overlappingTasks.map(function(t) { return t.verticalPosition; });
          
          // Find the first position that's not used
          var position = 0;
          while (usedPositions.indexOf(position) !== -1) {
            position++;
          }
          
          // Assign the position
          task.verticalPosition = position;
        }
        
        processedTasks.push(task);
      });
      
      ganttRows.push({
        name: username,
        tasks: processedTasks
      });
    });
    
    // Set the Gantt data
    $scope.ganttData = {
      data: ganttRows,
      timeScale: {
        from: minDate,
        to: maxDate
      }
    };
  }

  /**
   * Generate array of dates for the Gantt chart timeline
   */
  $scope.getTimelineDates = function(startDate, endDate) {
    var dates = [];
    var currentDate = new Date(startDate);
    var end = new Date(endDate);
    
    // Generate approximately 15 date steps between start and end
    var totalDays = Math.round((end - currentDate) / (1000 * 60 * 60 * 24));
    var step = Math.max(1, Math.round(totalDays / 15));
    
    while (currentDate <= end) {
      dates.push(new Date(currentDate));
      currentDate.setDate(currentDate.getDate() + step);
    }
    
    return dates;
  };

  /**
   * Calculate positioning style for a task in the Gantt chart
   */
  $scope.getTaskStyle = function(task, timeScale) {
    var startDate = new Date(task.start);
    var endDate = new Date(task.end);
    var timeScaleStart = new Date(timeScale.from);
    var timeScaleEnd = new Date(timeScale.to);
    
    // Calculate position as percentage of the timeline
    var totalTimeMs = timeScaleEnd - timeScaleStart;
    var startOffset = Math.max(0, startDate - timeScaleStart);
    var duration = Math.min(endDate - startDate, endDate - timeScaleStart);
    
    // Ensure task is visible even if it's very short
    duration = Math.max(duration, 36 * 60 * 60 * 1000); // Minimum 1.5 day width for better visibility
    
    var left = (startOffset / totalTimeMs) * 100;
    var width = (duration / totalTimeMs) * 100;
    
    // Ensure task fits within the timeline if it extends beyond
    if (left > 100) left = 100;
    if (left + width > 100) width = 100 - left;
    
    // Slight adjustment to prevent bars from touching the edges
    if (left < 0.5) left = 0.5;
    if (left + width > 99) width = 99 - left;
    
    // Calculate top position based on verticalPosition property
    var topPosition = 10 + (task.verticalPosition || 0) * 35; // 35px per level
    
    // Add color styling based on progress
    var backgroundColor = lightenColor(task.color, 30);
    var borderColor = darkenColor(task.color, 10);
    
    return {
      left: left + '%',
      width: width + '%',
      top: topPosition + 'px',
      backgroundColor: backgroundColor,
      borderColor: borderColor
    };
  };
  
  /**
   * Helper function to lighten a color
   */
  function lightenColor(color, percent) {
    if (!color) return '#f8f8f8';
    
    // Handle hex colors
    if (color.startsWith('#')) {
      var num = parseInt(color.slice(1), 16);
      var r = (num >> 16) + percent;
      var g = ((num >> 8) & 0x00FF) + percent;
      var b = (num & 0x0000FF) + percent;
      
      r = Math.min(r, 255);
      g = Math.min(g, 255);
      b = Math.min(b, 255);
      
      return '#' + (
        (r << 16) + 
        (g << 8) + 
        b
      ).toString(16).padStart(6, '0');
    }
    
    return color;
  }
  
  /**
   * Helper function to darken a color
   */
  function darkenColor(color, percent) {
    if (!color) return '#ddd';
    
    // Handle hex colors
    if (color.startsWith('#')) {
      var num = parseInt(color.slice(1), 16);
      var r = (num >> 16) - percent;
      var g = ((num >> 8) & 0x00FF) - percent;
      var b = (num & 0x0000FF) - percent;
      
      r = Math.max(r, 0);
      g = Math.max(g, 0);
      b = Math.max(b, 0);
      
      return '#' + (
        (r << 16) + 
        (g << 8) + 
        b
      ).toString(16).padStart(6, '0');
    }
    
    return color;
  }

  /**
   * Get color for a task based on progress.
   */
  function getTaskColor(progress) {
    if (progress === 100) {
      return '#5cb85c'; // Success/completed - green
    } else if (progress >= 70) {
      return '#5bc0de'; // Info/almost done - blue
    } else if (progress >= 30) {
      return '#f0ad4e'; // Warning/in progress - orange
    } else {
      return '#d9534f'; // Danger/not started - red
    }
  }

  /**
   * Apply filters to activities.
   */
  $scope.applyFilters = function() {
    $scope.offset = 0;
    $scope.loadActivities();
  };

  /**
   * Reset all filters.
   */
  $scope.resetFilters = function() {
    $scope.filterUser = null;
    $scope.filterType = null;
    $scope.offset = 0;
    $scope.loadActivities();
  };

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
   * Format activity type for display.
   */
  $scope.formatActivityType = function(type) {
    return formatActivityType(type);
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
        
        // Refresh Gantt data if needed
        if ($scope.showGantt) {
          prepareGanttData();
        }
      });
    }
  };

  // Initialize by loading activities
  $scope.loadActivities();
}); 