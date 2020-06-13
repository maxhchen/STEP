// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */

var all_greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!', 'Hallo Welt!', 'こんにちは世界', '안녕하세요 세계!', 'Привет мир!'];
var active_greetings = all_greetings.slice();

function addRandomGreeting() {
  // Pick a random greeting.
  const greeting = active_greetings[Math.floor(Math.random() * active_greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;

  // Prevent repeats before all messages are seen once.
  active_greetings.splice(active_greetings.indexOf(greeting), 1);
  if (active_greetings.length == 0) {
      active_greetings = all_greetings.slice();
  }
}

// Parses URL to determine number of comments to display.
function loadCommentsOnStart() {
  let commentLimit = (new URL(document.location)).searchParams.get('commentLimit');
  let languageCode = (new URL(document.location)).searchParams.get('languageCode');
  loadComments(commentLimit, languageCode);
}

// Function to identify which language to translate comments to.
function translateComments() {
    const options = document.getElementsByName('languageCode');
    var languageCode = "en";
    
    for (var i = 0; i < options.length; i++) {
        if (options[i].checked) {
            languageCode = options[i].value;
            break;
        }
    }

    let commentLimit = (new URL(document.location)).searchParams.get('commentLimit');
    loadComments(commentLimit, languageCode);
}

// Load `commentLimit` comments.
function loadComments(commentLimit, languageCode) {
    const URL = '/data?commentLimit=' + commentLimit + '&languageCode=' + languageCode;
    console.log(URL);
    fetch(URL).then(response => response.json()).then(allComments => {
    const commentContainer = document.getElementById('comment-container');

    // Prevent visual "glitch" when comments show up twice.
    commentContainer.innerHTML = "";

    allComments.forEach(commentText => {
      const commentItem = createCommentItem(commentText.text, commentText.timestamp, commentText.email);
      commentContainer.appendChild(commentItem);
    });
  });
}

// Helper function to instantiate each comment.
function createCommentItem(text, timestamp, email) {
  const commentItem = document.createElement('div');
  commentItem.innerText = "\"" + text + "\" sent on " + timestamp + " by " + email;
  commentItem.className = "comment-item";
  return commentItem;
}

// Get login status of user and either display hidden elements or display login form.
function fetchLoginStatus() {
    fetch("/auth").then(response => response.json()).then(status => {

        if (status[0] == "true") {
            const hiddenElements = document.getElementsByClassName('active-on-login');
            Array.from(hiddenElements).forEach(item => {
                item.style.display = "block";
            });
            const logoutLink = document.getElementById('status-link');
            const logoutButton = document.getElementById('status-button');
            logoutLink.href = status[1];
            logoutButton.innerText = "Logout here";

            const emailContainer = document.getElementById('email-container');
            emailContainer.innerText = "Welcome " + status[2] + "!";

        } else {
            const loginLink = document.getElementById('status-link');
            const loginButton = document.getElementById('status-button');
            loginLink.href = status[1];
            loginButton.innerText = "Login here";
        }
    });
}

// Loads Google Maps API.
function createMap() {
    const map = new google.maps.Map(document.getElementById('map-section'), {
        center: {lat: 37.579869, lng: -122.121974},
        zoom: 10,
        styles: [
                    {
                        "featureType": "landscape.natural",
                        "elementType": "geometry.fill",
                        "stylers": [{"visibility": "on"}, {"color": "#e0efef"}]
                    },
                
                    {
                        "featureType": "poi",
                        "elementType": "geometry.fill",
                        "stylers": [{"visibility": "on"}, {"hue": "#1900ff"}, {"color": "#c0e8e8"}]
                    },
                
                    {
                        "featureType": "road",
                        "elementType": "geometry",
                        "stylers": [{"lightness": 100}, {"visibility": "simplified"}]
                    },
                
                    {
                        "featureType": "road",
                        "elementType": "labels",
                        "stylers": [{"visibility": "off"}]
                    },
                
                    {
                        "featureType": "transit.line",
                        "elementType": "geometry",
                        "stylers": [{"visibility": "on"}, {"lightness": 700}]
                    },
                
                    {
                        "featureType": "water",
                        "elementType": "all",
                        "stylers": [{"color": "#7dcdcd"}]
                    }
                ]
    });
    // Styling is licensed under Creative Commons from https://snazzymaps.com/style/61/blue-essence.

  const homeMarker = new google.maps.Marker({
      position: {lat: 37.217832, lng: -121.858269},
      map: map,
      title: 'Home'
    });
  
  const freshmanMarker = new google.maps.Marker({
      position: {lat: 37.866276, lng: -122.255204},
      map: map,
      title: 'Freshman Year'
    });
  
  const sophomoreMarker = new google.maps.Marker({
      position: {lat: 37.867764, lng: -122.261153},
      map: map,
      title: 'Sophomore Year'
    });

  const juniorMarker = new google.maps.Marker({
      position: {lat: 37.867454, lng: -122.257261},
      map: map,
      title: 'Junior Year'
    });

  const schoolMarker = new google.maps.Marker({
      position: {lat: 37.871967, lng: -122.258583},
      map: map,
      title: 'School'
    });

  const otherHomeMarker = new google.maps.Marker({
      position: {lat: 25.034429, lng: 121.546511},
      map: map,
      title: 'Home away from Home'
    });
}

// Wrapper function to run multiple functions on page load.
function onLoad() {
    loadCommentsOnStart();
    fetchLoginStatus();
    createMap();
}
